package com.orangetalents.cadastro

enum class EnumTipoConta {
    CONTA_DESCONHECIDA {
        override fun codigoAssociadoBcb(): String = ""
    },
    CONTA_CORRENTE {
        override fun codigoAssociadoBcb(): String = "CACC"
    },
    CONTA_POUPANCA {
        override fun codigoAssociadoBcb(): String = "SVGS"
    };

    abstract fun codigoAssociadoBcb(): String
}