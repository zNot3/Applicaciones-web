package com.curso.android.module3.amiibo.domain.error


sealed class AmiiboError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    class Network(
        message: String = "Error de conexión. Verifica tu internet.",
        cause: Throwable? = null
    ) : AmiiboError(message, cause) {

        fun isTimeout(): Boolean {
            return cause?.message?.contains("timeout", ignoreCase = true) == true
        }
    }

    class Parse(
        message: String = "Error al procesar los datos del servidor.",
        cause: Throwable? = null
    ) : AmiiboError(message, cause)

    class Database(
        message: String = "Error al guardar los datos localmente.",
        cause: Throwable? = null
    ) : AmiiboError(message, cause)

    class Unknown(
        message: String = "Ocurrió un error inesperado.",
        cause: Throwable? = null
    ) : AmiiboError(message, cause)
}

enum class ErrorType {
    NETWORK,
    PARSE,
    DATABASE,
    UNKNOWN;

    companion object {
        fun from(error: AmiiboError): ErrorType = when (error) {
            is AmiiboError.Network -> NETWORK
            is AmiiboError.Parse -> PARSE
            is AmiiboError.Database -> DATABASE
            is AmiiboError.Unknown -> UNKNOWN
        }
    }
}
