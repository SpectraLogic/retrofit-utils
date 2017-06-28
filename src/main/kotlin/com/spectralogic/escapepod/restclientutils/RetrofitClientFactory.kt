package com.spectralogic.escapepod.restclientutils


interface RetrofitClientFactory {
    fun <T> createRestClient(endpoint : String, service : Class<T>, basePath: String = "") : T
}
