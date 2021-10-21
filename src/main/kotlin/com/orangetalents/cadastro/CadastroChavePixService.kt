package com.orangetalents.cadastro

import com.orangetalents.bcb.BankAccount
import com.orangetalents.bcb.BcbCliente
import com.orangetalents.bcb.CadastrarChaveBcbRequest
import com.orangetalents.bcb.Owner
import com.orangetalents.cadastro.dto.CadastroRequest;
import com.orangetalents.chavepix.ChavePix
import com.orangetalents.chavepix.ChavePixRepository;
import com.orangetalents.erp.ErpItauCliente
import jakarta.inject.Singleton;

import javax.transaction.Transactional;

@Singleton
open class CadastroService(val erpItauCliente: ErpItauCliente,
                      val bcbCliente: BcbCliente,
                      val chavePixRepository:ChavePixRepository) {

    @Transactional
    open fun registraChavePix(requestDto:CadastroRequest): ChavePix {
        var resposta = erpItauCliente.consulta(requestDto.tipoConta.name, requestDto.codigoInterno).body()
        var entidade = ChavePix(
            resposta.numero,
            requestDto.tipoConta,
            resposta.titular.id,
            requestDto.tipoChave,
            requestDto.valorChave
        )
        entidade = chavePixRepository.save(entidade)

        var respostaBcb = bcbCliente.cadastrar(
            CadastrarChaveBcbRequest(
                entidade.tipoChave.chaveAssociadaBcb(),
                entidade.valorChave,
                BankAccount(resposta.instituicao.ispb,resposta.agencia,resposta.numero,resposta.tipo.codigoAssociadoBcb()),
                Owner("NATURAL_PERSON",resposta.titular.nome,resposta.titular.cpf)
            )
        )

        if (requestDto.tipoChave == EnumTipoChave.ALEATORIA) {
            entidade.valorChave = respostaBcb.body().key
        }
        return entidade
    }
}
