package service

import dto.users.*
import exception.InvalidPasswordException
import exception.PasswordChangeException
import io.ktor.features.BadRequestException
import io.ktor.features.NotFoundException
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.UserModel
import org.springframework.security.crypto.password.PasswordEncoder
import repository.UserRepository
import java.lang.Exception

@KtorExperimentalAPI
class UserService(
    private val repo: UserRepository,
    private val tokenService: JWTTokenService,
    private val passwordEncoder: PasswordEncoder
) {
    private val mutex = Mutex()

    suspend fun getModelById(id: Long): UserModel? {
        return repo.getById(id)
    }

    suspend fun getByUserName(username: String): UserModel? {
        return repo.getByUsername(username)
    }

    suspend fun getById(id: Long): UserResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return UserResponseDto.fromModel(model)
    }

    suspend fun changePassword(id: Long, input: PasswordChangeRequestDto) {
        mutex.withLock {
            val model = repo.getById(id) ?: throw NotFoundException()
            if (!passwordEncoder.matches(input.old, model.password)) {
                throw PasswordChangeException("Wrong password!")
            }
            val copy = model.copy(password = passwordEncoder.encode(input.new))
            repo.save(copy)
        }
    }

    suspend fun authenticate(input: AuthenticationRequestDto): AuthenticationResponseDto {
        val model = repo.getByUsername(input.username) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.password, model.password)) {
            throw InvalidPasswordException("Wrong password!")
        }

        val token = tokenService.generate(model.id)
        return AuthenticationResponseDto(token)
    }

    suspend fun register(input: RegistrationRequestDto): RegistrationResponseDto {
        if (repo.getByUsername(input.username) == null) {
            repo.save(UserModel(username = input.username, password = passwordEncoder.encode(input.password)))
            val model = repo.getByUsername(input.username)
            val token = tokenService.generate(model!!.id)
            return RegistrationResponseDto(token)
        } else throw BadRequestException("Пользователь с таким логином уже зарегистрирован")
    }

    suspend fun save(username: String, password: String) {
        repo.save(UserModel(username = username, password = passwordEncoder.encode(password)))
        return
    }
}