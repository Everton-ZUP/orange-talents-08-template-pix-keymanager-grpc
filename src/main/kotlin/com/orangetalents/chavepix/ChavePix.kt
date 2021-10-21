package com.orangetalents.chavepix

import com.orangetalents.TipoConta
import com.orangetalents.cadastro.EnumTipoChave
import com.orangetalents.cadastro.EnumTipoConta
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:NotBlank val numeroConta: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) val tipoConta: EnumTipoConta,
    @field:NotBlank val idTitular: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) val tipoChave: EnumTipoChave,
    @field:NotBlank @field:Size(max = 77) @field:Column(unique = true) var valorChave: String
) {

    @Id
    var id : String = UUID.randomUUID().toString()
        private set

    fun pertenceAoTitular(titularId: String?): Boolean = titularId == idTitular
}
