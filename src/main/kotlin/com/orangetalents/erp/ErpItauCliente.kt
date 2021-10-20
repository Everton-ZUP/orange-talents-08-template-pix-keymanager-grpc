package com.orangetalents.erp

import com.orangetalents.erp.dto.ErpContaReply
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("\${ERPITAU.URL.CONTAS.CLIENTES}")
interface ErpItauCliente {

    @Get("/{id}/contas")
    fun consulta(@QueryValue tipo: String, @PathVariable id: String) : HttpResponse<ErpContaReply>
}