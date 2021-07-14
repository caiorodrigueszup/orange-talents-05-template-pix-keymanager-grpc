package br.com.zup.edu.pix.compartilhado.grpc.handlers

import br.com.zup.edu.pix.compartilhado.grpc.ExceptionHandler
import br.com.zup.edu.pix.compartilhado.grpc.ExceptionHandler.StatusWithDetails
import br.com.zup.edu.pix.compartilhado.grpc.exceptions.ChavePixExistenteException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExistenteExceptionHandler: ExceptionHandler<ChavePixExistenteException> {
    override fun handle(e: ChavePixExistenteException): StatusWithDetails {
        return StatusWithDetails(
            Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixExistenteException
    }
}