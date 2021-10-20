package com.orangetalents.cadastro.chavepix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix,UUID> {
    fun findByValorChave(valorChave: String) : ChavePix?
    fun existsByValorChave(valorChave: String): Boolean
}