package io.skygear.plugins.chat.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.squareup.picasso.Picasso
import io.skygear.chatkit.commons.ImageLoader
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.util.Base64
import com.squareup.picasso.Transformation

private val DISPLAY_IMAGE_SIZE = 500.0

class ImageLoader(
        val context: Context,
        val avatarBuilder: AvatarBuilder
) : ImageLoader {

    override fun loadImage(imageView: ImageView?, url: String?) {
        if (url == null) {
            return
        }

        // Load from avatar builder
        if (this.avatarBuilder.isValidAvatarBuilderUri(url)) {
            val bm = this.avatarBuilder.avatarForUri(url)
            imageView?.setImageBitmap(bm)
            return
        }

        var creator = Picasso.with(this.context)
                .load(url)
        creator.into(imageView)
    }

    fun loadImage(imageView: ImageView?, url: String?, thumbnail: String?, width: Int?, height: Int?, orientation: Int?) {
        // Load chat image message
        var creator = Picasso.with(this.context)
                .load(url)

        var h = height?.toDouble() ?: 0.0
        var w = width?.toDouble() ?: 0.0
        if (0 < h && 0 < w) {
            if (h > DISPLAY_IMAGE_SIZE || w > DISPLAY_IMAGE_SIZE) {
                var ratio = h / w
                if (ratio > 1) {
                    h = DISPLAY_IMAGE_SIZE
                    w = DISPLAY_IMAGE_SIZE / ratio
                } else {
                    h = DISPLAY_IMAGE_SIZE * ratio
                    w = DISPLAY_IMAGE_SIZE
                }
            }
            imageView?.layoutParams?.height = h.toInt()
            imageView?.layoutParams?.width = w.toInt()
            creator.fit().centerInside()
        }

        val imageDataBytes = thumbnail
        if (imageDataBytes != null) {
            val bytes = Base64.decode(imageDataBytes.toByteArray(), Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                creator.placeholder(BitmapDrawable(this.context.resources, bitmap))
            }
        }

        var o = orientation ?: ExifInterface.ORIENTATION_NORMAL
        var matrix = matrixFromRotation(o)

        matrix?.let {
            creator.transform(object : Transformation {
                override fun key(): String {
                    return "orientation"
                }

                override fun transform(source: Bitmap?): Bitmap {
                    val bmRotated = Bitmap.createBitmap(source, 0, 0, source!!.width, source!!.height, matrix, true)
                    source?.recycle()
                    return bmRotated
                }
            })
        }

        creator.into(imageView)
    }
}
