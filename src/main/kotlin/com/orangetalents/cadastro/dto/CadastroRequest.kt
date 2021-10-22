package com.orangetalents.cadastro.dto

import com.orangetalents.CadastrarChavePixRequest
import com.orangetalents.TipoChave
import com.orangetalents.TipoConta
import com.orangetalents.cadastro.EnumTipoChave
import com.orangetalents.cadastro.EnumTipoConta
import io.micronaut.core.annotation.Introspected
import java.lang.IllegalArgumentException
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class CadastroRequest(
    @field:NotBlank var codigoInterno: String?,
    @field:NotNull var tipoConta: EnumTipoConta,
    @field:NotNull var tipoChave: EnumTipoChave,
    @field:Size(max = 77) var valorChave: String?
) {

    constructor(request: CadastrarChavePixRequest) :
            this(request.codigoInterno,
                EnumTipoConta.valueOf(request.tipoConta.name),
                EnumTipoChave.valueOf(request.tipoChave.name), request.valorChave) {

        if (tipoConta == EnumTipoConta.CONTA_DESCONHECIDA) throw IllegalArgumentException("Tipo de conta desconhecida")
        if (request.tipoChave == TipoChave.CHAVE_DESCONHECIDA) throw IllegalArgumentException("Tipo de chave desconhecido")
        if (tipoChave.validacao(valorChave)) else throw IllegalArgumentException("Formato da chave informada está invalido")
        if (tipoChave==EnumTipoChave.ALEATORIA) valorChave = "1"//UUID.randomUUID().toString()
    }
}