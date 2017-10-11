package io.skygear.plugins.chat.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by carmenlau on 10/2/17.
 */

private val THUMBNAIL_SIZE= 100.0
private val IMAGE_SIZE = 1600.0
var mCurrentPhotoPath: String = ""

data class ImageData(val thumbnail: Bitmap,
                     val image: Bitmap)

fun getResizedBitmap(context: Context, uri: Uri): ImageData? {
    var input = context.getContentResolver().openInputStream(uri)

    val onlyBoundsOptions = BitmapFactory.Options()
    onlyBoundsOptions.inJustDecodeBounds = true
    onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888//optional
    BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
    input.close()

    if (onlyBoundsOptions.outWidth == -1 || onlyBoundsOptions.outHeight == -1) {
        return null
    }

    val originalSize = if (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth)
        onlyBoundsOptions.outHeight else onlyBoundsOptions.outWidth

    val imgRatio = if (originalSize > IMAGE_SIZE) originalSize / IMAGE_SIZE else 1.0
    val bitmap = getBitmap(context, uri, imgRatio)

    val thumbRatio = if (originalSize > THUMBNAIL_SIZE) originalSize / THUMBNAIL_SIZE else 1.0
    val thumbBitmap = getBitmap(context, uri, thumbRatio)

    return ImageData(thumbBitmap, bitmap)
}

fun getBitmap(context: Context, uri: Uri, ratio: Double) : Bitmap {
    val bitmapOptions = BitmapFactory.Options()
    bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio)
    bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888

    val input = context.getContentResolver().openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
    input.close()

    return bitmap
}

fun bitmapToByteArray(bmp: Bitmap): ByteArray? {
    val stream = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 70, stream)
    return stream.toByteArray()
}

@Throws(IOException::class)
fun createImageFile(context: Context): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
    )

    // Save a file: path for use with ACTION_VIEW intents
    mCurrentPhotoPath = image.getAbsolutePath()
    return image
}

private fun getPowerOfTwoForSampleRatio(ratio: Double): Int {
    val k = Integer.highestOneBit(Math.floor(ratio).toInt())
    return if (k == 0) 1 else k
}