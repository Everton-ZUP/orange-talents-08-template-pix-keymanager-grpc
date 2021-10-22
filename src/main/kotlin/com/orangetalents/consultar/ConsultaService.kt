package com.orangetalents.consultar

import com.google.protobuf.Timestamp
import com.orangetalents.ConsultaReply
import com.orangetalents.ConsultaReply.ChavePixRequest.*
import com.orangetalents.ConsultaRequest
import com.orangetalents.bcb.BcbCliente
import com.orangetalents.bcb.ChaveBcbReply
import com.orangetalents.bcb.Instituicoes
import com.orangetalents.chavepix.ChavePix
import com.orangetalents.chavepix.ChavePixRepository
import com.orangetalents.erp.ErpItauCliente
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.time.ZoneId

@Singleton
class ConsultaService(
    val chavePixRepository: ChavePixRepository,
    val bcbCliente: BcbCliente,
    val erpItauCliente: ErpItauCliente
) {
    fun consultaPorChave(chave: String): ConsultaReply {

        var respostaBcb = bcbCliente.buscar(chave)
        if (respostaBcb.status != HttpStatus.OK) {
            throw IllegalArgumentException("Chave não encontrada no Sistema do Banco central Brasileiro")
        }
        var chaveBcbReply = respostaBcb.body()
        return gerarConsultaReply(chaveBcbReply, null)
    }

    fun consultaPorPixId(pixId: ConsultaRequest.ConsultaRequestInterno): ConsultaReply {

        var chavePixInterna = chavePixRepository.findById(pixId.pixId)
            .orElseThrow{IllegalArgumentException("Chave Pix Não encontrada!")}
        var respostaBcb = bcbCliente.buscar(chavePixInterna.valorChave)
        if (respostaBcb.status != HttpStatus.OK) {
            throw IllegalArgumentException("Chave não encontrada no Sistema do Banco central Brasileiro")
        }
        return gerarConsultaReply(respostaBcb.body(),chavePixInterna)
    }

    fun consultaInvalida(): ConsultaReply {
        throw IllegalArgumentException("Consulta Invalida")
    }

    private fun gerarConsultaReply(chaveBcbReply: ChaveBcbReply, chavePixInterna: ChavePix?): ConsultaReply {
        return ConsultaReply
            .newBuilder()
            .setPixId(chavePixInterna?.id ?: "")
            .setClienteId(chavePixInterna?.idTitular ?: "")
            .setChave(
                newBuilder()
                    .setChave(chaveBcbReply.key)
                    .setTipoChave(chaveBcbReply.keyType)
                    .setConta(
                        ContaInfo
                            .newBuilder()
                            .setTipo(chaveBcbReply.bankAccount.accountType)
                            .setInstituicao(Instituicoes.nome(chaveBcbReply.bankAccount.participant))
                            .setNomeDoTitular(chaveBcbReply.owner.name)
                            .setCpfDoTitular(chaveBcbReply.owner.taxIdNumber)
                            .setAgencia(chaveBcbReply.bankAccount.branch)
                            .setNumeroDaConta(chaveBcbReply.bankAccount.accountNumber)
                            .build()
                    )
                    .setCriadaEm(chaveBcbReply.createdAt.toGrpcTimeStamp())
            )
            .build()
    }
}

private fun LocalDateTime.toGrpcTimeStamp(): Timestamp {
    return this.let {
        val instant = it.atZone(ZoneId.of("UTC-0")).toInstant()
        Timestamp.newBuilder().setSeconds(instant.epochSecond).setNanos(instant.nano).build()
    }
}
