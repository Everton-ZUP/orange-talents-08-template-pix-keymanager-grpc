package com.orangetalents.cadastro

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator


enum class EnumTipoChave {
    CHAVE_DESCONHECIDA {
        override fun validacao(chave: String?): Boolean {
            return false
        }
    },
    CPF {
        override fun validacao(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false
            return chave.matches("^[0-9]{11}\$".toRegex())
        }
    },
    CELULAR {
        override fun validacao(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun validacao(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false
            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    ALEATORIA {
        override fun validacao(chave: String?): Boolean {
            return chave.isNullOrBlank()
        }
    };

    abstract fun validacao(chave: String?): Boolean
}
