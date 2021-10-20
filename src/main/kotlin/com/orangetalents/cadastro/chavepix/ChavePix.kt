package com.orangetalents.cadastro.chavepix

import com.orangetalents.TipoConta
import com.orangetalents.cadastro.EnumTipoChave
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:NotBlank val numeroConta: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) val tipoConta: TipoConta,
    @field:NotBlank val idTitular: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) val tipoChave: EnumTipoChave,
    @field:NotBlank @field:Size(max = 77) @field:Column(unique = true) val valorChave: String
) {
    @Id
    var id : UUID = UUID.randomUUID()
        private set
}
