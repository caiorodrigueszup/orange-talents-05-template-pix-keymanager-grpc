package br.com.zup.edu.pix.compartilhado.anotacoescustomizadas

import br.com.zup.edu.pix.novachave.ChavePixRequest
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "chave Pix inválida (\${validatedValue.tipo})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidPixKeyValidator: ConstraintValidator<ValidPixKey, ChavePixRequest> {
    override fun isValid(
        value: ChavePixRequest?,
        context: ConstraintValidatorContext?
    ): Boolean {
     if (value?.tipo == null) {
         return false
     }
        return value.tipo.valida(value.chave)
    }
}
