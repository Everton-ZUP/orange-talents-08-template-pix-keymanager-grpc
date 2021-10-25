package com.orangetalents.consultartodas

import com.orangetalents.ConsultarServiceGrpc
import com.orangetalents.ConsultarTodasAsChavesGrpc
import com.orangetalents.ListaChavePixRequest
import com.orangetalents.cadastro.EnumTipoChave
import com.orangetalents.cadastro.EnumTipoConta
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
import org.junit.Assert
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(transactional = false)
internal class ConsultarTodasEndpointTest(
    val repository: ChavePixRepository,
    val grpcConsultarTodas: ConsultarTodasAsChavesGrpc.ConsultarTodasAsChavesBlockingStub
){
    @BeforeEach fun init() {repository.deleteAll()}

    @Test fun `deve trazer todos as chaves registradas para determinado cliente`(){
        //cenario
        repository.saveAll(listaDeChaves())
        //acao
        var resposta = grpcConsultarTodas.lista(ListaChavePixRequest.newBuilder().setClienteId("123").build())
        //validacao
        assertEquals(resposta.chavesCount,3)
    }
    @Test fun `deve trazer lista vazia caso não tenha nenhuma chave para determinado cliente`(){
        //cenario
        //acao
        var resposta = grpcConsultarTodas.lista(ListaChavePixRequest.newBuilder().setClienteId("123").build())
        //validacao
        assertEquals(resposta.chavesCount,0)
    }
    @Test fun `deve retornar erro ao buscar cliente inexistente`(){
        //cenario
        //acao
        var resposta = assertThrows<StatusRuntimeException>{
            grpcConsultarTodas.lista(ListaChavePixRequest.newBuilder().setClienteId("").build())
        }
        //validacao
        assertEquals(resposta.status.code,Status.INVALID_ARGUMENT.code)
        assertEquals(resposta.status.description,"Cliente_id não pode ser nulo nem vazio")
    }


    
    private fun listaDeChaves(): List<ChavePix> {
        var lista: MutableList<ChavePix> = listOf(
            ChavePix("123", EnumTipoConta.CONTA_CORRENTE, "123",EnumTipoChave.CPF, "111"),
            ChavePix("123", EnumTipoConta.CONTA_CORRENTE, "123",EnumTipoChave.CPF, "222"),
            ChavePix("123", EnumTipoConta.CONTA_CORRENTE, "123",EnumTipoChave.CPF, "333"),
        ) as MutableList<ChavePix>
        return lista
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ConsultarTodasAsChavesGrpc.ConsultarTodasAsChavesBlockingStub{
            return ConsultarTodasAsChavesGrpc.newBlockingStub(channel)
        }
    }
}