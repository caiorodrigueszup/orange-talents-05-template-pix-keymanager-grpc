package br.com.zup.edu.pix.novachave

import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.TipoConta
import br.com.zup.edu.pix.entities.ChavePix
import br.com.zup.edu.pix.entities.ContaAssociada
import br.com.zup.edu.pix.repositories.ChavePixRepository
import br.com.zup.edu.pix.servicosexternos.ContasDeClientesNoItauClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
class CadastrarChaveEndpointTest {

    @field:Inject
    lateinit var chavePixRepository: ChavePixRepository

    @field:Inject
    lateinit var clientGrpc: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub

    @field:Inject
    lateinit var contasDeClientesNoItauClient: ContasDeClientesNoItauClient

    lateinit var registraChavePixRequest: RegistraChavePixRequest
    lateinit var dadosDaContaResponse: DadosDaContaResponse
    lateinit var chavePix: ChavePix

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()

        registraChavePixRequest = RegistraChavePixRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipo(enumValueOf("CPF"))
            .setChave("45755411840")
            .setTipoConta(enumValueOf("CONTA_CORRENTE"))
            .build()

        dadosDaContaResponse = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            InstituicaoResponse(nome = "ITAÚ UNIBANCO S.A."),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse(nome = "Rafael M C Ponte", cpf = "02467781054")
        )

        chavePix = ChavePix(
            idCliente = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            tipo = TipoDeChave.CPF,
            chave = "45755411840",
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                agencia = "0001",
                numeroDaConta = "291900",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                instituicao = "ITAÚ UNIBANCO S.A."
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `Deve cadastrar uma nova chave pix`() {
        // cenario
        Mockito.`when`(
            contasDeClientesNoItauClient.buscarContaPorTipo(
                id = registraChavePixRequest.idCliente,
                tipo = registraChavePixRequest.tipoConta
            )
        ).thenReturn(HttpResponse.ok(dadosDaContaResponse))

        // acao
        val response = clientGrpc.cadastrar(registraChavePixRequest)

        // validacao
        with(response) {
            assertEquals(registraChavePixRequest.idCliente, idCliente)
            assertTrue(idPix.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$".toRegex()))
            assertNotNull(idPix)
            assertTrue(chavePixRepository.existsByChave("45755411840"))
        }
    }

    @Test
    fun `nao deve cadastrar uma chave que ja existe no banco`() {
        // cenario
        chavePixRepository.save(chavePix)

        // acao
        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.cadastrar(registraChavePixRequest)
        }

        // validacao
        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '${registraChavePixRequest.chave}' existente.", status.description)
            assertEquals(1, chavePixRepository.count())
        }
    }

    @Test
    fun `nao deve cadastrar chave pix com id de cliente que nao existe`() {
        val registraChavePixRequestAlterada = registraChavePixRequest.newBuilderForType()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157999")
            .setTipo(enumValueOf("CPF"))
            .setChave("45755411840")
            .setTipoConta(enumValueOf("CONTA_CORRENTE"))
            .build()

        Mockito.`when`(
        contasDeClientesNoItauClient.buscarContaPorTipo(
            registraChavePixRequestAlterada.idCliente,
            registraChavePixRequestAlterada.tipoConta
        )).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.cadastrar(registraChavePixRequestAlterada)
        }

        assertEquals(Status.FAILED_PRECONDITION.code, exception.status.code)
        assertEquals("Cliente não existente!", exception.status.description)
    }

    @Test
    fun `nao deve cadastrar chave pix com campos vazios`() {
        // acao
        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.cadastrar(RegistraChavePixRequest.newBuilder().build())
        }

        // validacao
        with(exception){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @MockBean(ContasDeClientesNoItauClient::class)
    fun mockandoContasDeClientesNoItauClient(): ContasDeClientesNoItauClient {
        return Mockito.mock(ContasDeClientesNoItauClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun criarStubClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}