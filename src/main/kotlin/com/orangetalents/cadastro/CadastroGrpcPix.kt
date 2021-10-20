package com.orangetalents.cadastro

import com.google.protobuf.Any
import com.google.rpc.BadRequest
import com.google.rpc.Code
import com.orangetalents.CadastrarChavePixReply
import com.orangetalents.CadastrarChavePixRequest
import com.orangetalents.KeyManagerGRPCServiceGrpc
import com.orangetalents.cadastro.chavepix.ChavePix
import com.orangetalents.cadastro.chavepix.ChavePixRepository
import com.orangetalents.cadastro.dto.CadastroRequest
import com.orangetalents.compartilhada.tranformaErrosDeValidacaoEmStatusRuntimeException
import com.orangetalents.compartilhada.validaSeRequestENula
import com.orangetalents.erp.ErpItauCliente
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolation
import javax.validation.Validator

@Singleton
class CadastroGrpcPix(
    val erpItauCliente: ErpItauCliente,
    val validator: Validator,
    val chavePixRepository: ChavePixRepository
) : KeyManagerGRPCServiceGrpc.KeyManagerGRPCServiceImplBase() {

    private val LOOGER = LoggerFactory.getLogger(javaClass)

    // @Transactional
    override fun cadastrar(
        request: CadastrarChavePixRequest?,
        responseObserver: StreamObserver<CadastrarChavePixReply>?
    ) {

        if (validaSeRequestENula(request, responseObserver, LOOGER)) return

        try {
            //Validando regras
            val requestDto = CadastroRequest(request!!)
            val contraintViolations = validator.validate(requestDto)
            if (contraintViolations.isNotEmpty()) {
                val exception = tranformaErrosDeValidacaoEmStatusRuntimeException(contraintViolations, LOOGER)
                responseObserver!!.onError(exception)
                return
            }

            if (chavePixRepository.existsByValorChave(requestDto.valorChave)){
                responseObserver!!.onError(
                    Status.ALREADY_EXISTS.withDescription("Chave informada já está sendo utilizada!")
                        .asRuntimeException())
                return
            }

            // Verificando dados com o sistema ERP do Itau
            try {
                var resposta = erpItauCliente.consulta(requestDto.tipoConta.name, requestDto.codigoInterno).body()

                var entidade = ChavePix(resposta.numero,resposta.tipo,resposta.titular.id,requestDto.tipoChave, requestDto.valorChave)
                entidade = chavePixRepository.save(entidade)

                responseObserver!!.onNext(
                    CadastrarChavePixReply
                        .newBuilder()
                        .setPixId(entidade.id.toString())
                        .build())

                LOOGER.info("Chave criada com sucesso "+entidade.id.toString())
                responseObserver.onCompleted()

            } catch (exection: Exception) {
                responseObserver!!.onError(
                    Status.NOT_FOUND.withDescription("Conta não encontrada no sistema Itaú").asRuntimeException())
                LOOGER.warn("Erro na requisição ao sistema erp do banco ")
            }
        } catch (exception: IllegalArgumentException) {
            responseObserver!!.onError(Status.INVALID_ARGUMENT.withDescription(exception.message).asRuntimeException())
            return
        }
    }

}