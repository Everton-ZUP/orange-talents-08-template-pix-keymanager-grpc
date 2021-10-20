package com.orangetalents.compartilhada

import com.google.protobuf.Any
import com.google.rpc.BadRequest
import com.google.rpc.Code
import com.orangetalents.CadastrarChavePixReply
import com.orangetalents.CadastrarChavePixRequest
import com.orangetalents.cadastro.dto.CadastroRequest
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import javax.validation.ConstraintViolation

fun validaSeRequestENula(
    request: CadastrarChavePixRequest?,
    responseObserver: StreamObserver<CadastrarChavePixReply>?,
    LOOGER: Logger
): Boolean {
    if (request == null) {
        LOOGER.warn("Request Nula")
        responseObserver!!.onError(
            Status.UNKNOWN
                .withDescription("A Request não pode ser nula") // Descrição do erro
                .augmentDescription("...") // Detalhes do erro
                .asRuntimeException()
        )
        return true
    }
    return false
}


fun tranformaErrosDeValidacaoEmStatusRuntimeException(
    contraintViolations: MutableSet<ConstraintViolation<CadastroRequest>>,
    LOOGER: Logger
): StatusRuntimeException? {

    val violations = contraintViolations.map {
        BadRequest.FieldViolation
            .newBuilder()
            .setField(it.propertyPath.last().toString())
            .setDescription(it.message)
            .build()
    }

    val badRequest = BadRequest.newBuilder().addAllFieldViolations(violations).build() // FieldViolations

    LOOGER.warn("Requisição com dados inválidos: " + contraintViolations.toString())

    val statusProto = com.google.rpc.Status
        .newBuilder()
        .setCode(Code.INVALID_ARGUMENT.number)
        .setMessage("Parametros de entrada invalidos")
        .addDetails(Any.pack(badRequest))
        .build()

    return StatusProto.toStatusRuntimeException(statusProto)
}
