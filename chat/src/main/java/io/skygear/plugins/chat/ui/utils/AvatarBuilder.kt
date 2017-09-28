package io.skygear.plugins.chat.ui.utils

import android.graphics.*
import android.graphics.Shader.TileMode
import android.text.TextUtils
import java.net.URI
import java.util.*


class AvatarBuilder {
    companion object {

        private val Scheme = AvatarBuilder::class.java.canonicalName
        private val NameQueryKey = "name"

        val AvatarWidth = 100
        val AvatarHeight = 100
        val AvatarTextSize = 50

        fun isValidAvatarBuilderUri(uri: String)
                = URI(uri).scheme == AvatarBuilder.Scheme

        fun AvatarForUri(uri: String): Bitmap {
            val parsed = URI(uri)
            val queries = parsed.query
                    .split('&')
                    .map { it.split('=', limit = 2) }
                    .fold(hashMapOf(), fun(acc, item): HashMap<String, MutableList<String>> {
                        val itemKey = item[0]
                        val itemValue = item[1]
                        if (acc.containsKey(itemKey)) {
                            acc[itemKey]!!.add(itemValue)
                        } else {
                            acc[itemKey] = mutableListOf(itemValue)
                        }

                        return acc
                    })

            // TODO: cache the latest n bitmap
            val name = queries[NameQueryKey]?.first() as String
            val initials = TextUtils.join(
                    "",
                    name.split(' ', limit = 2).map { it.substring(0, 1) }
            )
            val gradientColors = listOf(
                    Color.argb(255, 0, 121, 210),
                    Color.argb(255, 3, 184, 194)
            )
            val bm = Bitmap.createBitmap(
                    AvatarBuilder.AvatarWidth,
                    AvatarBuilder.AvatarHeight,
                    Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bm)
            canvas.drawColor(gradientColors.first())

            // Draw the gradient background
            val gradient = LinearGradient(
                    AvatarBuilder.AvatarWidth * 0.5f,
                    AvatarBuilder.AvatarHeight * 0.0f,
                    AvatarBuilder.AvatarWidth * 0.5f,
                    AvatarBuilder.AvatarHeight * 1.0f,
                    gradientColors[0],
                    gradientColors[1],
                    TileMode.CLAMP
            )

            val gradientPaint = Paint()
            gradientPaint.style = Paint.Style.FILL
            gradientPaint.shader = gradient
            gradientPaint.flags = Paint.ANTI_ALIAS_FLAG
            canvas.drawRect(
                    0f,
                    0f,
                    AvatarBuilder.AvatarWidth * 1.0f,
                    AvatarBuilder.AvatarHeight * 1.0f,
                    gradientPaint
            )

            // Draw the text
            val textPaint = Paint()
            textPaint.color = Color.WHITE
            textPaint.style = Paint.Style.FILL
            textPaint.textSize = AvatarTextSize * 1.0f
            textPaint.flags = Paint.ANTI_ALIAS_FLAG

            val textBound = Rect()
            textPaint.getTextBounds(initials, 0, initials.length, textBound)

            val xPos = canvas.width * 0.5f - textBound.exactCenterX()
            val yPos = canvas.height * 0.5f - textBound.exactCenterY()
            canvas.drawText(initials, xPos, yPos, textPaint)

            return bm
        }

        fun AvatarUriForName(name: String)
                = "${AvatarBuilder.Scheme}://user?name=$name"
    }
}
