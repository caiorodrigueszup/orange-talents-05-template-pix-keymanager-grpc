package br.com.zup.edu.pix.novachave

import br.com.zup.edu.TipoConta
import br.com.zup.edu.pix.compartilhado.anotacoescustomizadas.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class ChavePixRequest(
    @field:ValidUUID
    @field:NotBlank
    val idCliente: String?,

    @field:NotNull
    val tipo: TipoChave?,

    @field:Size(max = 77)
    val chave: String?,

    @field:NotNull
    val tipoConta: TipoConta?
) {

    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            idCliente = UUID.fromString(idCliente),
            tipo = TipoChave.valueOf(tipo!!.name),
            chave = if (this.tipo == TipoChave.ALEATORIA) UUID.randomUUID().toString() else chave!!,
            tipoConta = TipoConta.valueOf(tipoConta!!.name),
            conta = conta
        )
    }
}