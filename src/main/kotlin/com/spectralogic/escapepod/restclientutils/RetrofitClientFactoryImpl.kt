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

import com.spectralogic.escapepod.util.json.Mapper
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClientFactoryImpl : RetrofitClientFactory {
    override fun <T> createXmlRestClient(endpoint: String, service : Class<T>, basePath : String)
            = innerCreateClient(endpoint, service, basePath, SimpleXmlConverterFactory.create())

    override fun <T> createJsonRestClient(endpoint: String, service : Class<T>, basePath : String)
            = innerCreateClient(endpoint, service, basePath, JacksonConverterFactory.create(Mapper.mapper))

    private fun <T> innerCreateClient(endpoint: String, service: Class<T>, basePath: String, converterFactory: Converter.Factory): T {
        return Retrofit.Builder()
            .baseUrl(endpoint + basePath)
            .client(createOkioClient())
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(service)
    }

    private fun createOkioClient() : OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(LoggingInterceptor())

        //val httpLoggingInterceptor = HttpLoggingInterceptor()
        //httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        //builder.addInterceptor(httpLoggingInterceptor)

        builder.addInterceptor { chain ->

            val request = chain.request()
            val newRequest = request.newBuilder().addHeader("Content-Type", "application/xml")
                    .addHeader("Accepts", "application/xml")
                    .method(request.method(), request.body())
                    .build()

            chain.proceed(newRequest)
        }

        builder.connectTimeout(90L, TimeUnit.SECONDS)
        builder.readTimeout(90L, TimeUnit.SECONDS)
        builder.writeTimeout(90L, TimeUnit.SECONDS )

        return builder.build()
    }
}
