package com.orangetalents.cadastro

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator


enum class EnumTipoChave {
    CHAVE_DESCONHECIDA {
        override fun validacao(chave: String?): Boolean {
            return false
        }
        override fun chaveAssociadaBcb(): String = ""
    },
    CPF {
        override fun validacao(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false
            return chave.matches("^[0-9]{11}\$".toRegex())
        }
        override fun chaveAssociadaBcb(): String = "CPF"
    },
    CELULAR {
        override fun validacao(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
        override fun chaveAssociadaBcb(): String = "PHONE"
    },
    EMAIL {
        override fun validacao(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false
            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
        override fun chaveAssociadaBcb(): String = "EMAIL"
    },
    ALEATORIA {
        override fun validacao(chave: String?): Boolean {
            return chave.isNullOrBlank()
        }
        override fun chaveAssociadaBcb(): String = "RANDOM"
    };

    abstract fun validacao(chave: String?): Boolean
    abstract fun chaveAssociadaBcb(): String
}
