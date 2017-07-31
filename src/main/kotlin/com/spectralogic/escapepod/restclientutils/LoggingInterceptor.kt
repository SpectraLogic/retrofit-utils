/*
 * *****************************************************************************
 *    Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
 *    Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *    this file except in compliance with the License. A copy of the License is located at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file.
 *    This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *    CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *    specific language governing permissions and limitations under the License.
 *  ****************************************************************************
 */

package com.spectralogic.escapepod.restclientutils

import com.spectralogic.escapepod.util.ifNotNull
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

class LoggingInterceptor : Interceptor {
    companion object {
        private val LOG = LoggerFactory.getLogger(LoggingInterceptor::class.java)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val t1 = System.nanoTime()
        LOG.info(String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()))

        request.body().ifNotNull {
            val bodyBuffer = Buffer()
            LOG.info("Body Info: Content-Length = {}, Content-Type: {}", it.contentLength(), it.contentType())
            it.writeTo(bodyBuffer)
            LOG.info("Body:\n{}", bodyBuffer.readString(Charset.forName("UTF-8")))

        }

        val response = chain.proceed(request)

        val t2 = System.nanoTime()
        LOG.info(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6, response.headers()))

        response.body().ifNotNull {
            if (it.contentLength() != 0L) {
                val source = it.source()
                source.request(Long.MAX_VALUE)
                LOG.info("Response Body:\n{}", source.buffer().clone().readString(Charset.forName("UTF-8")))
            }
        }

        return response
    }

}