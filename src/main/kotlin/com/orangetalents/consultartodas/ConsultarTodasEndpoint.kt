package com.orangetalents.consultartodas

import com.orangetalents.*
import com.orangetalents.chavepix.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton

@Singleton
class ConsultarTodasEndpoint(
    val repository: ChavePixRepository
) : ConsultarTodasAsChavesGrpc.ConsultarTodasAsChavesImplBase(){

    override fun lista(request: ListaChavePixRequest, responseObserver: StreamObserver<ListaChavePixResponse>) {
        try {
            if (request.clienteId.isNullOrBlank()) {throw IllegalArgumentException("Cliente_id não pode ser nulo nem vazio")}

            var chaves = repository.findAllByIdTitular(request.clienteId).map {
                ListaChavePixResponse.ChavesPix.newBuilder()
                .setPixId(it.id)
                    .setTipo(TipoChave.valueOf(it.tipoChave.name))
                    .setChave(it.valorChave)
                    .setComta(TipoConta.valueOf(it.tipoConta.name))
                .build()
            }

            responseObserver.onNext(
                ListaChavePixResponse.newBuilder()
                    .setClienteId(request.clienteId).addAllChaves(chaves).build())
            responseObserver.onCompleted()
        }catch (e : IllegalArgumentException){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException())
        }
    }
}