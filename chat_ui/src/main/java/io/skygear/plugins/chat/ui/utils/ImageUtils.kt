package io.skygear.plugins.chat.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.* // ktlint-disable no-wildcard-imports
import android.media.ExifInterface
import java.io.FileOutputStream

/**
 * Created by carmenlau on 10/2/17.
 */

private val THUMBNAIL_SIZE = 80.0
private val IMAGE_SIZE = 1600.0
var mCurrentPhotoPath: String = ""

data class ImageData(val thumbnail: Bitmap,
                     val image: Bitmap)

fun getImageOrientation(context: Context, uri: Uri): Int {
    var input = context.getContentResolver().openInputStream(uri)
    val file = File.createTempFile("image_tmp", ".jpg", context.getCacheDir())
    val fos = FileOutputStream(file)

    var len: Int
    val buffer = ByteArray(1024)

    do {
        len = input.read(buffer, 0, 1024)
        if (len == -1)
            break
        fos.write(buffer, 0, len)
    } while (true)
    input.close()
    fos.close()

    val ef = ExifInterface(file.toString())
    return ef.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
}

fun getResizedBitmap(context: Context, uri: Uri, orientation: Int): ImageData? {
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
    val bitmap = getBitmap(context, uri, imgRatio, orientation)

    val thumbRatio = if (originalSize > THUMBNAIL_SIZE) originalSize / THUMBNAIL_SIZE else 1.0
    val thumbBitmap = getBitmap(context, uri, thumbRatio, orientation)

    return ImageData(thumbBitmap, bitmap)
}

fun getBitmap(context: Context, uri: Uri, ratio: Double, orientation: Int): Bitmap {
    val bitmapOptions = BitmapFactory.Options()
    bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio)
    bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888

    val input = context.getContentResolver().openInputStream(uri)
    var bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)

    bitmap = rotateBitmap(bitmap, orientation)

    input.close()

    return bitmap
}

fun bitmapToByteArray(bmp: Bitmap): ByteArray? {
    val stream = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 70, stream)
    return stream.toByteArray()
}

fun matrixFromRotation(orientation: Int): Matrix? {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_NORMAL -> return null
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix?.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
            matrix.setRotate(180f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
        else -> return null
    }
    return matrix
}

fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {
    val matrix = matrixFromRotation(orientation)
    try {
        val bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return bmRotated
    } catch (e: OutOfMemoryError) {
        e.printStackTrace()
        return null
    }
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
            storageDir /* directory */
    )

    // Save a file: path for use with ACTION_VIEW intents
    mCurrentPhotoPath = image.getAbsolutePath()
    return image
}

private fun getPowerOfTwoForSampleRatio(ratio: Double): Int {
    val k = Integer.highestOneBit(Math.floor(ratio).toInt())
    return if (k == 0) 1 else k
}
