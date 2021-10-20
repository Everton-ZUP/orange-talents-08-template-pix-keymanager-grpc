package com.orangetalents.cadastro

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


internal class EnumTipoChaveTest{
    @Nested
    inner class ALEATORIA {
        @Test
        fun `chave aleatoria deve ser nula ou vazia`() {
            with(EnumTipoChave.ALEATORIA) {
                assertTrue(validacao(null))
                assertTrue(validacao(""))
            }
        }

        @Test
        fun `nao permite valor quando o tipo da chave for aleatorio`() {
            with(EnumTipoChave.ALEATORIA) {
                assertFalse(validacao("um valor"))
            }
        }

    }

    @Nested
    inner class CPF {

        @Test
        fun `deve ser valido quando chave cpf for um numero de 11 digitos`() {
            with(EnumTipoChave.CPF) {
                assertTrue(validacao("11111111111"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave cpf for um numero invalido`() {
            with(EnumTipoChave.CPF) {
                assertFalse(validacao("1111111111111"))
                assertFalse(validacao("123"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave cpf for nula ou vazio`() {
            with(EnumTipoChave.CPF) {
                assertFalse(validacao(null))
                assertFalse(validacao(""))
            }
        }

        @Test
        fun `nao deve ser valido quando chave cpf possuir letras`() {
            with(EnumTipoChave.CPF) {
                assertFalse(validacao("teste123"))
            }
        }


    }

    @Nested
    inner class CELULAR {

        @Test
        fun `deve ser valido quando chave celular for um numero valido`() {
            with(EnumTipoChave.CELULAR) {
                assertTrue(validacao("+5545111111111"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave celular for um numero invalido`() {
            with(EnumTipoChave.CELULAR) {
                assertFalse(validacao("32231031"))
                assertFalse(validacao("+55459982081062844")) // maior que 14 numeros
            }
        }

        @Test
        fun `nao deve ser valido quando chave celular for nulo ou vazio`() {
            with(EnumTipoChave.CELULAR) {
                assertFalse(validacao(null))
                assertFalse(validacao(""))
            }
        }

        @Test
        fun `nao deve ser valido quando chave celular possuir letras`() {
            with(EnumTipoChave.CELULAR) {
                assertFalse(validacao("+5545celular"))
            }
        }


    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve ser valido quando chave email for um email valido`() {
            with(EnumTipoChave.EMAIL) {
                assertTrue(validacao("teste@teste"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave email for um email invalido`() {
            with(EnumTipoChave.EMAIL) {
                assertFalse(validacao("teste"))
                assertFalse(validacao("teste.com"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave email for nulo ou vazio`() {
            with(EnumTipoChave.EMAIL) {
                assertFalse(validacao(null))
                assertFalse(validacao(""))
            }
        }

        @Test
        fun `nao deve ser valido quando chave email possuir dois arobas`() {
            with(EnumTipoChave.EMAIL) {
                assertFalse(validacao("teste@teste@testado.com"))
            }
        }


    }
}