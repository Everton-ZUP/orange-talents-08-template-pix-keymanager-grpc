package com.orangetalents.cadastro

import com.google.rpc.BadRequest
import com.orangetalents.CadastrarChavePixReply
import com.orangetalents.CadastrarChavePixRequest
import com.orangetalents.KeyManagerGRPCServiceGrpc
import com.orangetalents.cadastro.dto.CadastroRequest
import com.orangetalents.chavepix.ChavePixRepository
import com.orangetalents.compartilhada.tranformaErrosDeValidacaoEmStatusRuntimeException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.validation.Validator

@Singleton
class CadastroGrpcPix(
    val validator: Validator,
    val chavePixRepository: ChavePixRepository,
    val cadastroService: CadastroService
) : KeyManagerGRPCServiceGrpc.KeyManagerGRPCServiceImplBase() {

    private val LOOGER = LoggerFactory.getLogger(javaClass)

    // @Transactional
    override fun cadastrar(
        request: CadastrarChavePixRequest?,
        responseObserver: StreamObserver<CadastrarChavePixReply>?
    ) {

        try {
            //Validando regras
            val requestDto = CadastroRequest(request!!)
            val contraintViolations = validator.validate(requestDto)
            if (contraintViolations.isNotEmpty()) {
                val violations = contraintViolations.map {
                    BadRequest.FieldViolation
                        .newBuilder()
                        .setField(it.propertyPath.last().toString())
                        .setDescription(it.message)
                        .build()
                }
                val exception = tranformaErrosDeValidacaoEmStatusRuntimeException(violations, LOOGER)
                responseObserver!!.onError(exception)
                return
            }

            if (chavePixRepository.existsByValorChave(requestDto.valorChave!!)) {
                responseObserver!!.onError(
                    Status.ALREADY_EXISTS.withDescription("Chave informada já está sendo utilizada!")
                        .asRuntimeException()
                )
                return
            }

            // Verificando dados com o sistema ERP do Itau
            try {

                var entidade = cadastroService.registraChavePix(requestDto)

                responseObserver!!.onNext(
                    CadastrarChavePixReply
                        .newBuilder()
                        .setPixId(entidade.id.toString())
                        .build()
                )

                LOOGER.info("Chave criada com sucesso " + entidade.id.toString())
                responseObserver.onCompleted()

            } catch (exection: Exception) {
                responseObserver!!.onError(
                    Status.NOT_FOUND.withDescription("Conta não encontrada no sistema Itaú").asRuntimeException()
                )
                LOOGER.warn("Erro na requisição ao sistema erp do banco "+exection.message)
            }
        } catch (exception: IllegalArgumentException) {
            responseObserver!!.onError(Status.INVALID_ARGUMENT.withDescription(exception.message).asRuntimeException())
            return
        }
    }
}