package com.orangetalents.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${BCB.URL}")
interface BcbCliente {

    @Post(consumes = [MediaType.APPLICATION_XML], processes = [MediaType.APPLICATION_XML])
    fun cadastrar(@Body request: CadastrarChaveBcbRequest): HttpResponse<ChaveBcbReply>

    @Delete("/{id}", consumes = [MediaType.APPLICATION_XML], processes = [MediaType.APPLICATION_XML])
    fun deletar(@PathVariable id: String, @Body request: DeletarChaveBcbRequest): HttpResponse<DeletarChaveBcbReply>

    @Get("/{id}", consumes = [MediaType.APPLICATION_XML])
    fun buscar(@PathVariable id: String): HttpResponse<ChaveBcbReply>
}

data class CadastrarChaveBcbRequest(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
)
data class Owner(
    val type: String,
    val name: String,
    val taxIdNumber: String,
)
data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String
)
data class ChaveBcbReply(
    val keyType: String,
    val key: String,
    val createdAt : LocalDateTime
)

data class DeletarChaveBcbReply(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class DeletarChaveBcbRequest(
    val key: String,
    val participant: String
)