package example.relay.source

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import java.security.MessageDigest
import org.json.JSONObject

/** Minimal Relay source extension: one bounded JSON request/reply over an explicit Binder service. */
class RelayExtensionService : Service() {
    override fun onBind(intent: Intent): IBinder = object : Binder() {
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code != RELAY_REQUEST || reply == null || !trustedRelayCaller()) return false
            if (data.dataSize() > MAX_MESSAGE_BYTES) return false
            val request = data.readString().orEmpty()
            if (request.toByteArray().size > MAX_MESSAGE_BYTES) return false
            val requestJson = runCatching { JSONObject(request) }.getOrNull() ?: return false
            val result = when (requestJson.optString("method")) {
                "handshake" -> JSONObject()
                    .put("id", "example.relay.source")
                    .put("version", "1.0.1-test")
                    .put("kind", "SOURCE")
                    .put("api", JSONObject().put("minimum", 1).put("maximum", 1))
                    .put("capabilities", listOf("browse"))
                    .put("permissions", listOf("NETWORK"))
                    .put("settingsSchemaVersion", 1)
                    .put("authentication", listOf("NONE"))
                "browse" -> JSONObject().put("tracks", emptyList<Any>())
                else -> null
            }
            val response = if (result == null) {
                JSONObject().put("id", requestJson.optString("id")).put("error", "Unknown method.")
            } else {
                JSONObject().put("id", requestJson.optString("id")).put("result", result)
            }
            val payload = response.toString()
            if (payload.toByteArray().size > MAX_MESSAGE_BYTES) return false
            reply.writeString(payload)
            return true
        }
    }

    private fun trustedRelayCaller(): Boolean {
        @Suppress("DEPRECATION")
        val metadata = runCatching {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        }.getOrNull()
        val hostPackage = metadata?.getString("relay.host.package").orEmpty()
        val expectedCertificate = metadata?.getString("relay.host.certificateSha256").orEmpty()
        if (hostPackage.isBlank() || !expectedCertificate.matches(Regex("[0-9a-f]{64}"))) return false
        val callerPackages = packageManager.getPackagesForUid(Binder.getCallingUid()).orEmpty()
        if (hostPackage !in callerPackages) return false
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= 28) {
                packageManager.getPackageInfo(hostPackage, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(hostPackage, PackageManager.GET_SIGNATURES)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            return false
        }
        @Suppress("DEPRECATION")
        val signatures = if (Build.VERSION.SDK_INT >= 28) packageInfo.signingInfo?.apkContentsSigners.orEmpty() else packageInfo.signatures.orEmpty()
        return signatures.any { signature ->
            certificateSha256(signature.toByteArray()) == expectedCertificate
        }
    }

    private fun certificateSha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private companion object {
        const val RELAY_REQUEST = IBinder.FIRST_CALL_TRANSACTION
        const val MAX_MESSAGE_BYTES = 64 * 1024
    }
}
