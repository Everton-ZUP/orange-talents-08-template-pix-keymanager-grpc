package com.orangetalents.deletar

import com.orangetalents.DeletarChavePixReply
import com.orangetalents.DeletarChavePixRequest
import com.orangetalents.DeletarGRPCServiceGrpc
import com.orangetalents.KeyManagerGRPCServiceGrpc
import com.orangetalents.bcb.BcbCliente
import com.orangetalents.bcb.DeletarChaveBcbRequest
import com.orangetalents.chavepix.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*

@Singleton
class DeletarGrpcPix(
    val chavePixRepository: ChavePixRepository,
    val bcbCliente: BcbCliente
) :
    DeletarGRPCServiceGrpc.DeletarGRPCServiceImplBase() {

    private val LOOGER = LoggerFactory.getLogger(javaClass)

    override fun deletar(request: DeletarChavePixRequest?, responseObserver: StreamObserver<DeletarChavePixReply>?) {

        try {
            var chave = chavePixRepository.findById(request!!.chavePixId)
                .orElseThrow { IllegalArgumentException("Chave Não encontrada") }

            try {
                if (chave.pertenceAoTitular(request.titularId)) {

                    var respBcb = bcbCliente.deletar(chave.valorChave, DeletarChaveBcbRequest(chave.valorChave,"60701190"))
                    if (respBcb.status != HttpStatus.OK){
                        LOOGER.warn("Erro ao remover chave do bcb "+ respBcb.status+ respBcb.toString())
                        throw IllegalArgumentException("Erro ao remover chave do Banco central do Brasil")
                    }
                    chavePixRepository.deleteById(chave.id.toString())
                    responseObserver!!.onNext(
                        DeletarChavePixReply.newBuilder().setMensagem("Chave Pix deletada com sucesso!").build()
                    )
                    LOOGER.info("Chave pix deletada - chave pix " + request.chavePixId + " : titular " + request.titularId)
                    responseObserver.onCompleted()
                } else {
                    throw IllegalArgumentException("Chave não pertence ao titular informado!")
                }
            } catch (exception: IllegalArgumentException) {
                responseObserver!!.onError(
                    Status.FAILED_PRECONDITION.withDescription(exception.message).asRuntimeException()
                )
                LOOGER.warn(exception.message + " - " + request.chavePixId + " : " + request.titularId)
            }
        } catch (exception: IllegalArgumentException) {
            responseObserver!!.onError(
                Status.NOT_FOUND.withDescription(exception.message).asRuntimeException()
            )
            LOOGER.warn(exception.message + " - " + request!!.chavePixId)
        }
    }
}