/*
 * ****************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
 * ***************************************************************************
 */

package com.spectralogic.retrofitutils

import com.fasterxml.jackson.databind.ObjectMapper
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class RetrofitClientFactoryImpl : RetrofitClientFactory {
    override fun <T> createXmlRestClient(endpoint: String, service: Class<T>, basePath: String, userAgent: String) =
        innerCreateClient(endpoint, service, basePath, SimpleXmlConverterFactory.create(), "application/xml", userAgent)

    override fun <T> createJsonRestClient(
        endpoint: String,
        service: Class<T>,
        basePath: String,
        userAgent: String,
        insecure: Boolean,
        mapper: ObjectMapper
    ) =
        innerCreateClient(
            endpoint,
            service,
            basePath,
            JacksonConverterFactory.create(mapper),
            "application/json",
            userAgent,
            insecure
        )

    override fun <T> createJsonRestClientWithBearer(
        endpoint: String,
        service: Class<T>,
        basePath: String,
        userAgent: String,
        insecure: Boolean,
        bearer: String,
        mapper: ObjectMapper
    ) =
        innerCreateClient(
            endpoint,
            service,
            basePath,
            JacksonConverterFactory.create(mapper),
            "application/json",
            userAgent,
            insecure,
            bearer
        )

    private fun <T> innerCreateClient(
        endpoint: String,
        service: Class<T>,
        basePath: String,
        converterFactory: Converter.Factory,
        contentType: String,
        userAgent: String,
        insecure: Boolean = false,
        bearer: String? = null
    ): T {
        return Retrofit.Builder()
            .baseUrl(endpoint + basePath)
            .client(createOkioClient(contentType, userAgent, insecure, bearer))
            .addConverterFactory(converterFactory)
            .build()
            .create(service)
    }

    private fun createOkioClient(
        contentType: String,
        userAgent: String,
        insecure: Boolean,
        bearer: String?
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(LoggingInterceptor())
        builder.addInterceptor { chain ->

            val request = chain.request()
            val newRequest =
                request.newBuilder().addHeader("Content-Type", contentType)
                    .addHeader("Accepts", contentType)
                    .addHeader("User-Agent", userAgent)
                    .let {
                        if (bearer != null) {
                            it.addHeader("Authorization", "Bearer $bearer")
                        } else {
                            it
                        }
                    }
                    .method(request.method(), request.body())
                    .build()

            chain.proceed(newRequest)
        }

        if (insecure) {
            val (sslSocketFactory, trustManager) = insecureSSLContext()
            builder.sslSocketFactory(sslSocketFactory, trustManager)
            builder.hostnameVerifier { _, _ -> true }
        }
        builder.connectTimeout(90L, TimeUnit.SECONDS)
        builder.readTimeout(90L, TimeUnit.SECONDS)
        builder.writeTimeout(90L, TimeUnit.SECONDS)

        return builder.build()
    }

    private fun insecureSSLContext(): Pair<SSLSocketFactory, X509TrustManager> {
        // Create a trust manager that does not validate certificate chains

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        })

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        // Create an ssl socket factory with our all-trusting manager
        return Pair(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
    }
}
