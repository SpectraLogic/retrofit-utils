/*
 * ****************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
 * ***************************************************************************
 */

package com.spectralogic.escapepod.restclientutils

interface RetrofitClientFactory {
    fun <T> createXmlRestClient(
        endpoint: String,
        service: Class<T>,
        basePath: String = "",
        userAgent: String = "RioBroker"
    ): T

    fun <T> createJsonRestClient(
        endpoint: String,
        service: Class<T>,
        basePath: String = "",
        userAgent: String = "RioBroker",
        insecure: Boolean = false
    ): T

    fun <T> createJsonRestClientWithBearer(
        endpoint: String,
        service: Class<T>,
        basePath: String = "",
        userAgent: String = "RioBroker",
        insecure: Boolean = false,
        bearer: String
    ): T
}
