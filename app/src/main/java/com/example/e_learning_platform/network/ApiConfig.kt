package com.example.e_learning_platform.network

import com.example.e_learning_platform.BuildConfig

object ApiConfig {

    /**
     * Domyślny adres URL serwera API.
     * Na emulatorze Androida '10.0.2.2' wskazuje na 'localhost' komputera hosta.
     * Na fizycznym urządzeniu zastąp go adresem IP komputera w sieci lokalnej (np. http://192.168.1.50:8000/).
     */
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

    @Volatile
    var customBaseUrl: String? = null

    @Volatile
    var forceMockApi: Boolean? = null

    val baseUrl: String
        get() {
            val custom = customBaseUrl?.trim()
            if (!custom.isNullOrEmpty()) {
                return if (custom.endsWith("/")) custom else "$custom/"
            }
            val buildConfigUrl = runCatching { BuildConfig.API_BASE_URL }.getOrNull()?.trim()
            if (!buildConfigUrl.isNullOrEmpty()) {
                return if (buildConfigUrl.endsWith("/")) buildConfigUrl else "$buildConfigUrl/"
            }
            return DEFAULT_BASE_URL
        }

    val useMockApi: Boolean
        get() {
            forceMockApi?.let { return it }
            return runCatching { BuildConfig.USE_MOCK_API }.getOrDefault(false)
        }
}
