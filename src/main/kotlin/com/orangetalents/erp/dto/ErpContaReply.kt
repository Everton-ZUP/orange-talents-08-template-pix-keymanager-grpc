package com.orangetalents.erp.dto

import com.orangetalents.TipoConta

data class ErpContaReply(
    val tipo: TipoConta,
    val agencia: String,
    val numero: String,
    val instituicao: ErpInstituicaoReply,
    val titular: ErpTitularReply
)