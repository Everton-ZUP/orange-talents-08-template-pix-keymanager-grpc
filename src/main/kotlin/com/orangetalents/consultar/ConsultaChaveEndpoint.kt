package com.orangetalents.consultar

import com.orangetalents.ConsultaReply
import com.orangetalents.ConsultaRequest
import com.orangetalents.ConsultarServiceGrpc
import com.orangetalents.bcb.BcbCliente
import com.orangetalents.chavepix.ChavePixRepository
import com.orangetalents.erp.ErpItauCliente
import io.grpc.Status
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton

@Singleton
class ConsultaChaveEndpoint(
    val consultaService: ConsultaService
): ConsultarServiceGrpc.ConsultarServiceImplBase() {
    override fun consultar(request: ConsultaRequest?, responseObserver: StreamObserver<ConsultaReply>?) {

        try {
            var resultado = request!!.buscaDadosResponse(consultaService)
            responseObserver!!.onNext(resultado)
            responseObserver.onCompleted()
        }catch (exception: Exception){
            responseObserver!!.onError(Status.INVALID_ARGUMENT.withDescription(exception.message).asRuntimeException())
        }
    }
}