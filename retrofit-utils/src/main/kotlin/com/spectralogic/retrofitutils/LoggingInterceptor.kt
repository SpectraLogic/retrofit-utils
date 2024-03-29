/*
 * ****************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
 * ***************************************************************************
 */

package com.spectralogic.retrofitutils

import java.nio.charset.Charset
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import org.slf4j.LoggerFactory

class LoggingInterceptor : Interceptor {
    companion object {
        private val LOG = LoggerFactory.getLogger(LoggingInterceptor::class.java)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val t1 = System.nanoTime()
        LOG.info(
            String.format(
                "Sending request %s %s on %s%n%s",
                request.method(), request.url(), chain.connection(), request.headers()
            )
        )

        request.body()?.let {
            val bodyBuffer = Buffer()
            LOG.info("Body Info: Content-Length = {}, Content-Type: {}", it.contentLength(), it.contentType())
            it.writeTo(bodyBuffer)
            LOG.info("Body:\n{}", bodyBuffer.readString(Charset.forName("UTF-8")))
        }

        val response = chain.proceed(request)

        val t2 = System.nanoTime()
        LOG.info(
            String.format(
                "Received %d response for %s %s in %.1fms%n%s",
                response.code(), request.method(), response.request().url(), (t2 - t1) / 1e6, response.headers()
            )
        )

        response.body()?.let {
            if (it.contentLength() != 0L) {
                val source = it.source()
                source.request(Long.MAX_VALUE)
                LOG.info("Response Body:\n{}", source.buffer().clone().readString(Charset.forName("UTF-8")))
            }
        }

        return response
    }
}
