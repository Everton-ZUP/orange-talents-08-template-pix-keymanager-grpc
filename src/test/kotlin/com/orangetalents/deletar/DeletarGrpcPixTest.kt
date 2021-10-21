package com.orangetalents.deletar

import com.orangetalents.DeletarChavePixRequest
import com.orangetalents.DeletarGRPCServiceGrpc
import com.orangetalents.TipoConta
import com.orangetalents.cadastro.EnumTipoChave
import com.orangetalents.chavepix.ChavePix
import com.orangetalents.chavepix.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(transactional = false)
internal class DeletarGrpcPixTest(
    val chavePixRepository: ChavePixRepository,
    val grpcDeletar: DeletarGRPCServiceGrpc.DeletarGRPCServiceBlockingStub
){

    @BeforeEach
    fun setup() = chavePixRepository.deleteAll()

    @Test
    fun `deve deletar uma chave pix com sucesso`(){
        //cenario
        var chaveNoBanco = chavePixRepository.save(
            ChavePix(
                "1234",
                TipoConta.CONTA_CORRENTE,
                "1234",
                EnumTipoChave.CPF,
                "11111111111"
            )
        )
        //acao
        val resposta = grpcDeletar.deletar(
            DeletarChavePixRequest.newBuilder()
                .setChavePixId(chaveNoBanco.id)
                .setTitularId(chaveNoBanco.idTitular)
                .build())
        //validacao

        resposta.run {
            Assertions.assertEquals("Chave Pix deletada com sucesso!",mensagem)
            Assertions.assertFalse(chavePixRepository.existsById(chaveNoBanco.id))
        }
    }

    @Test
    fun `deve dar erro not found ao tentar deletar uma chave que não existe no banco`(){
        //cenario

        //acao
        val erro = assertThrows<StatusRuntimeException> {
            grpcDeletar.deletar(
                DeletarChavePixRequest.newBuilder()
                    .setChavePixId("123")
                    .setTitularId("123")
                    .build()
            )
        }
        //validacao
        with(erro){
            Assertions.assertEquals(Status.NOT_FOUND.code, status.code)
            Assertions.assertEquals("Chave Não encontrada",status.description)
        }
    }

    @Test
    fun `deve dar erro failed precondition ao tentar deletar uma chave que não pertence ao titular informado`(){
        //cenario
        var chaveNoBanco = chavePixRepository.save(
            ChavePix(
                "1234",
                TipoConta.CONTA_CORRENTE,
                "1234",
                EnumTipoChave.CPF,
                "11111111111"
            )
        )
        //acao
        val erro = assertThrows<StatusRuntimeException> {
            grpcDeletar.deletar(
                DeletarChavePixRequest.newBuilder()
                    .setChavePixId(chaveNoBanco.id)
                    .setTitularId("titularErrado")
                    .build()
            )
        }
        //validacao
        with(erro){
            Assertions.assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            Assertions.assertEquals("Chave não pertence ao titular informado!",status.description)
        }
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): DeletarGRPCServiceGrpc.DeletarGRPCServiceBlockingStub {
            return DeletarGRPCServiceGrpc.newBlockingStub(channel)
        }
    }
}