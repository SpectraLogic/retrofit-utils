/*
 * ****************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
 * ***************************************************************************
 */

package com.spectralogic.retrofitutils

import com.fasterxml.jackson.databind.ObjectMapper

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
        insecure: Boolean = false,
        mapper: ObjectMapper = ObjectMapper()
    ): T

    fun <T> createJsonRestClientWithBearer(
        endpoint: String,
        service: Class<T>,
        basePath: String = "",
        userAgent: String = "RioBroker",
        insecure: Boolean = false,
        bearer: String,
        mapper: ObjectMapper = ObjectMapper()
    ): T
}
