package com.orangetalents.erp.dto

import com.orangetalents.cadastro.EnumTipoConta

data class ErpContaReply(
    val tipo: EnumTipoConta,
    val agencia: String,
    val numero: String,
    val instituicao: ErpInstituicaoReply,
    val titular: ErpTitularReply
)