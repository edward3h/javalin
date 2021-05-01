package io.javalin.http.staticfiles

import io.javalin.Javalin
import io.javalin.core.util.Util
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.JavalinResponseWrapper
import org.eclipse.jetty.server.Request

class StaticFileHandler(config: StaticFileConfig) : Handler {
    val handler = PrefixableHandler(config);
    override fun handle(ctx: Context) {
        val httpRequest = ctx.req
        val httpResponse = ctx.res
        val target = httpRequest.getAttribute("jetty-target") as String
        val baseRequest = httpRequest.getAttribute("jetty-request") as Request
        try {
            val resource = handler.getResource(target)
            if (resource.isFile() || resource.isDirectoryWithWelcomeFile(handler, target)) {
                handler.config.headers.forEach { httpResponse.setHeader(it.key, it.value) }
                if (handler.config.precompress && PrecompressingResourceHandler.handle(resource, httpRequest, httpResponse)) {
                    return
                }
                httpResponse.contentType = null // Jetty will only set the content-type if it's null
                handler.handle(target, baseRequest, httpRequest, httpResponse)
                httpRequest.setAttribute("handled-as-static-file", true)
                (httpResponse as JavalinResponseWrapper).outputStream.finalize()
            }
        } catch (e: Exception) {
            ctx.status(404)
            if (!Util.isClientAbortException(e)) {
                Javalin.log?.info("Exception occurred while handling static resource", e)
            }
        }
    }
}
