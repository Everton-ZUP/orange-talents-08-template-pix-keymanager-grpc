package com.orangetalents.consultar

import com.orangetalents.ConsultaRequest
import com.orangetalents.ConsultarServiceGrpc
import com.orangetalents.KeyManagerGRPCServiceGrpc
import com.orangetalents.bcb.*
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
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import java.time.LocalDateTime

@MicronautTest(transactional = false)
internal class ConsultaChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcCliert: ConsultarServiceGrpc.ConsultarServiceBlockingStub
){
    @Inject
    lateinit var bcbCliente: BcbCliente

    @BeforeEach fun init(){repository.deleteAll()}

    @Test fun `deve retornar dados da chave pix buscando por pix_id valido` (){
        //cenario
        var chaveDoBanco = repository.save(
            ChavePix("12345",EnumTipoConta.CONTA_CORRENTE,"12345",EnumTipoChave.CPF,"11111111111"))
            mockaRespostaBCB("11111111111")
        //acao
        var respostaRequisicaoGRPC = grpcCliert.consultar(
            ConsultaRequest.newBuilder()
                .setPixId(
                    ConsultaRequest.ConsultaRequestInterno
                        .newBuilder().setPixId(chaveDoBanco.id).setClientId("12345").build())
                .build())
        //validacao
        with(respostaRequisicaoGRPC){
            assertEquals(clienteId,chaveDoBanco.idTitular)
            assertEquals(this.chave.chave,chaveDoBanco.valorChave)
            assertEquals(chaveDoBanco.tipoConta.codigoAssociadoBcb(),this.chave.conta.tipo)
        }
    }
    @Test fun `não deve retornar dados da chave pix buscando por pix_id com titular diferente` (){
        //cenario
        var chaveDoBanco = repository.save(
            ChavePix("12345",EnumTipoConta.CONTA_CORRENTE,"12345",EnumTipoChave.CPF,"11111111111"))
            mockaRespostaBCB("11111111111")
        //acao
        var respostaRequisicaoGRPC = assertThrows<StatusRuntimeException>{
            grpcCliert.consultar(
            ConsultaRequest.newBuilder()
                .setPixId(
                    ConsultaRequest.ConsultaRequestInterno
                        .newBuilder().setPixId(chaveDoBanco.id).setClientId("teste").build())
                .build())
        }
        //validacao
        with(respostaRequisicaoGRPC){
            assertEquals(this.status.code,Status.INVALID_ARGUMENT.code)
            assertEquals(this.status.description,"Titular não condiz com o informado!")
        }
    }
    @Test fun `não deve retornar dados da chave pix buscando por pix_id invalido` (){
        //cenario

        //acao
        var respostaRequisicaoGRPC = assertThrows<StatusRuntimeException>{
            grpcCliert.consultar(
            ConsultaRequest.newBuilder()
                .setPixId(
                    ConsultaRequest.ConsultaRequestInterno
                        .newBuilder().setPixId("12341231412341231423").setClientId("teste").build())
                .build())
        }
        //validacao
        with(respostaRequisicaoGRPC){
            assertEquals(this.status.code,Status.INVALID_ARGUMENT.code)
            assertEquals(this.status.description,"Chave Pix Não encontrada!")
        }
    }
    @Test fun `deve retornar dados da chave pix buscando por chave valida` (){
        //cenario
        var chaveDoBanco = repository.save(
            ChavePix("12345",EnumTipoConta.CONTA_CORRENTE,"12345",EnumTipoChave.CPF,"11111111111"))
        mockaRespostaBCB("11111111111")
        //acao
        var respostaRequisicaoGRPC = grpcCliert.consultar(
            ConsultaRequest.newBuilder()
                .setChave("11111111111")
                .build())
        //validacao
        with(respostaRequisicaoGRPC){
            assertEquals(this.chave.chave,chaveDoBanco.valorChave)
            assertEquals(chaveDoBanco.tipoConta.codigoAssociadoBcb(),this.chave.conta.tipo)
        }
    }
    @Test fun `deve retornar dados da chave pix buscando por chave valida no bcb mas não existe no nosso sistema` (){
        //cenario
        mockaRespostaBCB("11111111111")
        //acao
        var respostaRequisicaoGRPC = grpcCliert.consultar(
            ConsultaRequest.newBuilder()
                .setChave("11111111111")
                .build())
        //validacao
        with(respostaRequisicaoGRPC){
            assertEquals(this.chave.chave,"11111111111")
            assertEquals("CACC",this.chave.conta.tipo)
        }
    }


    fun mockaRespostaBCB(chave:String): OngoingStubbing<HttpResponse<ChaveBcbReply>>? {
        return Mockito.`when`(
            bcbCliente.buscar(chave)
            ).thenReturn(
                HttpResponse.ok(
                ChaveBcbReply(EnumTipoChave.CPF.name,"11111111111", LocalDateTime.now(),
                    BankAccount("60701190","12345","12345","CACC"),
                    Owner("NATURAL_PERSON","Test","11111111111")
                )
                ))
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ConsultarServiceGrpc.ConsultarServiceBlockingStub{
            return ConsultarServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BcbCliente::class)
    fun bcbCliente(): BcbCliente?{
        return Mockito.mock(BcbCliente::class.java)
    }
}