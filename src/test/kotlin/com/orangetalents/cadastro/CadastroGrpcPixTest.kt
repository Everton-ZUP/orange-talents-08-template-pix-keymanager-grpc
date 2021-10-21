package com.orangetalents.cadastro

import com.orangetalents.CadastrarChavePixRequest
import com.orangetalents.KeyManagerGRPCServiceGrpc
import com.orangetalents.TipoChave
import com.orangetalents.TipoConta
import com.orangetalents.bcb.*
import com.orangetalents.chavepix.ChavePix
import com.orangetalents.chavepix.ChavePixRepository
import com.orangetalents.erp.ErpItauCliente
import com.orangetalents.erp.dto.ErpContaReply
import com.orangetalents.erp.dto.ErpInstituicaoReply
import com.orangetalents.erp.dto.ErpTitularReply
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime

@MicronautTest(transactional = false)
internal class CadastroGrpcPixTest(
    val chavePixRepository: ChavePixRepository,
    val grpcCliente: KeyManagerGRPCServiceGrpc.KeyManagerGRPCServiceBlockingStub
) {

    @Inject
    lateinit var erpItauCliente: ErpItauCliente
    @Inject
    lateinit var bcbCliente: BcbCliente

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve cadastrar uma nova chave pix corretamente`() {
        // cenario
        Mockito.`when`(erpItauCliente.consulta(TipoConta.CONTA_CORRENTE.name, "1234"))
            .thenReturn(criaUmRetornoDoERPValido())

        // ação
        val resposta = grpcCliente.cadastrar(novaRequisicaoDeCadastrarChaveValida())

        //validacao
        val buscaChaveCriada = chavePixRepository.findByValorChave("11111111111")
        resposta.run {
            assertNotNull(pixId)
            assertEquals(buscaChaveCriada!!.id.toString(),pixId.toString())
        }
    }
    @Test
    fun `deve cadastrar uma nova chave aleatoria com chave do bcb`() {
        // cenario
        Mockito.`when`(erpItauCliente.consulta(EnumTipoConta.CONTA_CORRENTE.name, "1234"))
            .thenReturn(criaUmRetornoDoERPValido())
        Mockito.`when`(
            bcbCliente.cadastrar(CadastrarChaveBcbRequest("RANDOM","1",
            BankAccount("1111","1234","1234","CACC"),
            Owner("NATURAL_PERSON","Test","11111111111"))))
            .thenReturn(HttpResponse.created(ChaveBcbReply(EnumTipoChave.ALEATORIA.name,"123499", LocalDateTime.now())))


        // ação
        val resposta = grpcCliente.cadastrar(CadastrarChavePixRequest
            .newBuilder()
            .setCodigoInterno("1234")
            .setTipoChave(TipoChave.ALEATORIA)
            .setValorChave("")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build())

        //validacao
        val buscaChaveCriada = chavePixRepository.findByValorChave("123499")
        resposta.run {
            assertNotNull(pixId)
            assertEquals(buscaChaveCriada!!.id.toString(),pixId.toString())
        }
    }

    @Test
    fun `nao deve cadastrar chave duplicada` (){
        //cenario
        var chaveNoBanco = chavePixRepository.save(
            ChavePix(
                "1234",
                EnumTipoConta.CONTA_CORRENTE,
                "1234",
                EnumTipoChave.CPF,
                "11111111111"
            )
        )

        //açao
        try{
            grpcCliente.cadastrar(novaRequisicaoDeCadastrarChaveValida())
        }catch (exception : StatusRuntimeException){
          //validacao
            with(exception) {
                assertEquals(Status.ALREADY_EXISTS.code, status.code)
                assertEquals("Chave informada já está sendo utilizada!", status.description)
            }
        }
    }

    @Test
    fun `nao deve cadastrar uma chave pix quando cliente inexistente`() {
        // cenario
        Mockito.`when`(erpItauCliente.consulta(TipoConta.CONTA_CORRENTE.name, "1234")).thenReturn(
            HttpResponse.notFound())

        //açao
        try{
            grpcCliente.cadastrar(novaRequisicaoDeCadastrarChaveValida())
        }catch (exception : StatusRuntimeException){
            //validacao
            with(exception) {
                assertEquals(Status.NOT_FOUND.code, status.code)
                assertEquals("Conta não encontrada no sistema Itaú", status.description)
            }
        }
    }

    @Test
    fun `nao deve cadastrar uma chave pix quando parametros forem invalidos`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcCliente.cadastrar(CadastrarChavePixRequest.newBuilder().build())
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    private fun novaRequisicaoDeCadastrarChaveValida(): CadastrarChavePixRequest? {
        return CadastrarChavePixRequest
            .newBuilder()
            .setCodigoInterno("1234")
            .setTipoChave(TipoChave.CPF)
            .setValorChave("11111111111")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()
    }

    private fun criaUmRetornoDoERPValido(): HttpResponse<ErpContaReply>? {
        return HttpResponse.ok(ErpContaReply(
            EnumTipoConta.CONTA_CORRENTE,
            "1234",
            "1234",
            ErpInstituicaoReply("Teste","1111"),
            ErpTitularReply("1234","Test","11111111111")
        ))
    }


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGRPCServiceGrpc.KeyManagerGRPCServiceBlockingStub {
            return KeyManagerGRPCServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ErpItauCliente::class)
    fun erpItauClient(): ErpItauCliente?{
        return Mockito.mock(ErpItauCliente::class.java)
    }
    @MockBean(BcbCliente::class)
    fun bcbCliente(): BcbCliente?{
        return Mockito.mock(BcbCliente::class.java)
    }
}