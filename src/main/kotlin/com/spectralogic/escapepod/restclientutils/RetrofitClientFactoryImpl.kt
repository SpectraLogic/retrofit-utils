/*
 * ****************************************************************************
 *   Copyright 2014-2018 Spectra Logic Corporation. All Rights Reserved.
 * ***************************************************************************
 */

package com.spectralogic.escapepod.restclientutils

import com.spectralogic.escapepod.util.json.Mapper
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClientFactoryImpl : RetrofitClientFactory {
    override fun <T> createXmlRestClient(endpoint: String, service: Class<T>, basePath: String, userAgent: String) =
        innerCreateClient(endpoint, service, basePath, SimpleXmlConverterFactory.create(), "application/xml", userAgent)

    override fun <T> createJsonRestClient(endpoint: String, service: Class<T>, basePath: String, userAgent: String) =
        innerCreateClient(
            endpoint,
            service,
            basePath,
            JacksonConverterFactory.create(Mapper.mapper),
            "application/json",
            userAgent
        )

    override fun <T> createJsonRestClientWithBearer(endpoint: String, service: Class<T>, basePath: String, userAgent: String, bearer: String) =
        innerCreateClient(
            endpoint,
            service,
            basePath,
            JacksonConverterFactory.create(Mapper.mapper),
            "application/json",
            userAgent,
            bearer
        )

    private fun <T> innerCreateClient(
        endpoint: String,
        service: Class<T>,
        basePath: String,
        converterFactory: Converter.Factory,
        contentType: String,
        userAgent: String,
        bearer: String? = null
    ): T {
        return Retrofit.Builder()
            .baseUrl(endpoint + basePath)
            .client(createOkioClient(contentType, userAgent, bearer))
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(service)
    }

    private fun createOkioClient(contentType: String, userAgent: String, bearer: String?): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(LoggingInterceptor())

        // val httpLoggingInterceptor = HttpLoggingInterceptor()
        // httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        // builder.addInterceptor(httpLoggingInterceptor)

        builder.addInterceptor { chain ->

            val request = chain.request()
            val newRequest = request.newBuilder().addHeader("Content-Type", contentType)
                .addHeader("Accepts", contentType)
                .addHeader("User-Agent", userAgent)
                    .let {
                        if(bearer != null) {
                            it.addHeader("Authorization", "Bearer $bearer")
                        } else {
                            it
                        }
                    }
                .method(request.method(), request.body())
                .build()

            chain.proceed(newRequest)
        }

        builder.connectTimeout(90L, TimeUnit.SECONDS)
        builder.readTimeout(90L, TimeUnit.SECONDS)
        builder.writeTimeout(90L, TimeUnit.SECONDS)

        return builder.build()
    }
}
