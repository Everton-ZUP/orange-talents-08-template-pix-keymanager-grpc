package com.orangetalents.chavepix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix,String> {
    fun findByValorChave(valorChave: String) : ChavePix?
    fun existsByValorChave(valorChave: String): Boolean
    fun findAllByIdTitular(clienteId: String): List<ChavePix>
}