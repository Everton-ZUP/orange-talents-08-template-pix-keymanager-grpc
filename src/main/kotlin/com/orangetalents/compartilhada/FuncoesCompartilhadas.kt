package com.orangetalents.compartilhada

import com.google.protobuf.Any
import com.google.rpc.BadRequest
import com.google.rpc.Code
import com.orangetalents.cadastro.dto.CadastroRequest
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import org.slf4j.Logger
import javax.validation.ConstraintViolation

fun tranformaErrosDeValidacaoEmStatusRuntimeException(
    violations: List<BadRequest.FieldViolation>,
    LOOGER: Logger
): StatusRuntimeException? {

    val badRequest = BadRequest.newBuilder().addAllFieldViolations(violations).build() // FieldViolations

    LOOGER.warn("Requisição com dados inválidos: " + violations.toString())

    val statusProto = com.google.rpc.Status
        .newBuilder()
        .setCode(Code.INVALID_ARGUMENT.number)
        .setMessage("Parametros de entrada invalidos")
        .addDetails(Any.pack(badRequest))
        .build()

    return StatusProto.toStatusRuntimeException(statusProto)
}
