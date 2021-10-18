package com.orangetalents.cadastro

import com.orangetalents.CadastrarChavePixReply
import com.orangetalents.CadastrarChavePixRequest
import com.orangetalents.KeyManagerGRPCServiceGrpc
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton

@Singleton
class CadastroGrpcPix : KeyManagerGRPCServiceGrpc.KeyManagerGRPCServiceImplBase() {
    override fun cadastrar(
        request: CadastrarChavePixRequest?,
        responseObserver: StreamObserver<CadastrarChavePixReply>?
    ) {



        var retorno:CadastrarChavePixReply = CadastrarChavePixReply.newBuilder().setPixId("TODO:").build()
        responseObserver!!.onNext(retorno)
        responseObserver.onCompleted()
    }
}