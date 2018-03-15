package io.skygear.plugins.chat.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import io.skygear.plugins.chat.ui.utils.getImageOrientation
import io.skygear.plugins.chat.ui.utils.matrixFromRotation

class ImagePreviewActivity : AppCompatActivity() {

    companion object {
        val ImageURLIntentKey = "IMAGE_URL"

        fun newIntent(context: Context, imageURL: String): Intent {
            val intent = Intent(context, ImagePreviewActivity::class.java)
            intent.putExtra(ImageURLIntentKey, imageURL)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        title = ""

        var url = intent.getStringExtra(ImageURLIntentKey)
        val fromContentResolver = url.startsWith("content")
        if (fromContentResolver) {
            url = url.substring(0, url.indexOf("?"))
        }
        val creator = Picasso.with(this)
                .load(url)

        if (fromContentResolver) {
            val orientation = getImageOrientation(this, Uri.parse(url))
            var matrix = matrixFromRotation(orientation)

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
        }
        creator.into(findViewById<ImageView>(R.id.iv_photo))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.image_preview, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.close -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
