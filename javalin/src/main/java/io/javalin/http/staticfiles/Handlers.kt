package io.javalin.http.staticfiles

import io.javalin.core.util.Header
import io.javalin.http.Handler
import java.util.function.Consumer

object Handlers {

    fun staticFiles(directory: String, location: Location): Handler {
        return staticFiles { staticFiles ->
            staticFiles.directory = directory
            staticFiles.location = location
        }
    }

    fun webJars(): Handler {
        return staticFiles { staticFiles: StaticFileConfig ->
            staticFiles.directory = "/webjars"
            staticFiles.headers[Header.CACHE_CONTROL] = "max-age=31622400"
        }

    }

    fun staticFiles(userConfig: Consumer<StaticFileConfig>): Handler {
        val finalConfig = StaticFileConfig()
        userConfig.accept(finalConfig)
        return StaticFileHandler(finalConfig)
    }

}
