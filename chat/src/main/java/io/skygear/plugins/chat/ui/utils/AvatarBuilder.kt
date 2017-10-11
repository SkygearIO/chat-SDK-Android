package io.skygear.plugins.chat.ui.utils

import android.graphics.*
import android.graphics.Shader.TileMode
import android.text.TextUtils
import java.net.URI
import java.util.*


class AvatarBuilder(
        val avatarWidth: Int,
        val avatarHeight: Int,
        val avatarTextSize: Int,
        cacheSize: Int? = null
) {
    companion object {
        private val Scheme = AvatarBuilder::class.java.canonicalName
        private val NameQueryKey = "name"
        private var sharedInstance: AvatarBuilder? = null

        val DefaultAvatarWidth = 100
        val DefaultAvatarHeight = 100
        val DefaultAvatarTextSize = 50

        fun defaultBuilder(): AvatarBuilder {
            if (sharedInstance == null) {
                sharedInstance = AvatarBuilder()
            }

            return sharedInstance as AvatarBuilder
        }
    }

    constructor() : this(
            AvatarBuilder.DefaultAvatarWidth,
            AvatarBuilder.DefaultAvatarHeight,
            AvatarBuilder.DefaultAvatarTextSize
    )

    private var cache: AvatarCache

    init {
        this.cache = AvatarCache(cacheSize)
    }

    fun isValidAvatarBuilderUri(uri: String)
            = URI(uri).scheme == AvatarBuilder.Scheme

    fun avatarForUri(uri: String): Bitmap {
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

        val name = queries[NameQueryKey]?.first() as String
        val initials = TextUtils.join(
                "",
                name.split(' ', limit = 2).map(fun(str): String {
                    if (str.isNotEmpty()) {
                        return str.substring(0, 1)
                    }

                    return ""
                })
        )

        // retrieve from cache if available
        val cachedBitmap = this.cache.get(initials)
        if (cachedBitmap != null) {
            return cachedBitmap
        }

        val gradientColors = listOf(
                Color.argb(255, 0, 121, 210),
                Color.argb(255, 3, 184, 194)
        )
        val bm = Bitmap.createBitmap(
                this.avatarWidth,
                this.avatarHeight,
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bm)
        canvas.drawColor(gradientColors.first())

        // Draw the gradient background
        val gradient = LinearGradient(
                this.avatarWidth * 0.5f,
                this.avatarHeight * 0.0f,
                this.avatarWidth * 0.5f,
                this.avatarHeight * 1.0f,
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
                this.avatarWidth * 1.0f,
                this.avatarHeight * 1.0f,
                gradientPaint
        )

        // Draw the text
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = this.avatarTextSize * 1.0f
        textPaint.flags = Paint.ANTI_ALIAS_FLAG

        val textBound = Rect()
        textPaint.getTextBounds(initials, 0, initials.length, textBound)

        val xPos = canvas.width * 0.5f - textBound.exactCenterX()
        val yPos = canvas.height * 0.5f - textBound.exactCenterY()
        canvas.drawText(initials, xPos, yPos, textPaint)

        this.cache.add(initials, bm)

        return bm
    }

    fun avatarUriForName(name: String)
            = "${AvatarBuilder.Scheme}://user?name=$name"

    class AvatarCache(cacheSize: Int?) {
        companion object {
            val DefaultCacheSize = 20
        }

        val cacheSize: Int

        private var items: PriorityQueue<AvatarCacheItem>

        init {
            if (cacheSize != null && cacheSize > 0) {
                this.cacheSize = cacheSize
            } else {
                this.cacheSize = DefaultCacheSize
            }
            this.items = PriorityQueue(this.cacheSize)
        }

        fun get(name: String): Bitmap? {
            val idx = this.items.indexOfFirst { it.name == name }
            if (idx == -1) {
                return null
            }

            val found = this.items.elementAt(idx)

            this.items.remove(found)
            this.items.add(AvatarCacheItem(
                    name = found.name,
                    content = found.content,
                    lastHit = Date()
            ))

            return found.content
        }

        fun add(name: String, content: Bitmap) {
            val found = this.items.find { it.name == name }
            found?.let { this@AvatarCache.items.remove(it) }
            this.items.add(AvatarCacheItem(
                    name = name,
                    content = content,
                    lastHit = Date()
            ))

            while (this.items.size > this.cacheSize) {
                this.items.remove(this.items.last())
            }
        }

        private data class AvatarCacheItem(
                val name: String,
                val content: Bitmap,
                val lastHit: Date
        ): Comparable<AvatarCacheItem> {
            override operator fun compareTo(other: AvatarCacheItem)
                = -1 * this.lastHit.compareTo(other.lastHit)
        }
    }
}
