package io.skygear.plugins.chat.ui

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * PermissionManager encapsulates the complicated permission request flow
 */
abstract class PermissionManager(
        val activityContext: Activity,
        val permissions: List<String>,
        val permissionGrantedHandler: (() -> Unit)? = null,
        val permissionDeniedHandler: ((
                permissionsDenied: List<String>,
                neverAskAgain: Boolean
        ) -> Unit)? = null
) {
    private var permissionsNeverAskAgain = false

    abstract fun request(permissions: List<String>)

    private val deniedPermissions: List<String>
        get() = this.permissions.filter {
            ContextCompat.checkSelfPermission(this.activityContext, it) ==
                    PackageManager.PERMISSION_DENIED
        }

    fun notifyRequestResult(
            permissions: List<String>,
            grantResults: List<Int>
    ) {
        val granted = grantResults.isNotEmpty() &&
                grantResults.first() == PackageManager.PERMISSION_GRANTED
        if (granted) {
            this.permissionGrantedHandler?.invoke()
        } else {
            this.permissionsNeverAskAgain = permissions.filter {
                ActivityCompat.shouldShowRequestPermissionRationale(
                        this@PermissionManager.activityContext,
                        it
                ).not()
            }.any()
            this.permissionDeniedHandler?.invoke(
                    permissions,
                    this.permissionsNeverAskAgain
            )
        }
    }

    fun permissionsGranted() = this.deniedPermissions.isEmpty()

    fun runIfPermissionGranted(toRun: () -> Unit) {
        val permissionsDenied = this.deniedPermissions

        when {
            permissionsDenied.isEmpty() -> toRun()
            this.permissionsNeverAskAgain ->
                this.permissionDeniedHandler
                        ?.invoke(
                                permissionsDenied,
                                this.permissionsNeverAskAgain
                        )
            else -> this.request(permissionsDenied)
        }
    }
}