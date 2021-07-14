package br.com.zup.edu.pix.novachave

import br.com.zup.edu.pix.compartilhado.grpc.ErrorHandle
import br.com.zup.edu.pix.compartilhado.grpc.exceptions.ChavePixExistenteException
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauClient: ContasDeClientesNoItauClient
) {

    @Transactional
    fun registra(@Valid novaChavePix: ChavePixRequest): ChavePix {
        if (chavePixRepository.existsByChave(novaChavePix.chave!!)) {
             throw ChavePixExistenteException("Chave Pix '${novaChavePix.chave}' existente.")
        }

        val response = itauClient.buscarContaPorTipo(novaChavePix.idCliente!!, novaChavePix.tipoConta!!)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não existente!")

        val chavePix = novaChavePix.toModel(conta)
        chavePixRepository.save(chavePix)

        return chavePix
    }
}