package service

import dto.users.AuthenticationRequestDto
import dto.users.AuthenticationResponseDto
import dto.users.PasswordChangeRequestDto
import dto.users.UserResponseDto
import exception.InvalidPasswordException
import exception.PasswordChangeException
import io.ktor.features.NotFoundException
import io.ktor.util.KtorExperimentalAPI
import model.UserModel
import org.springframework.security.crypto.password.PasswordEncoder
import repository.UserRepository

@KtorExperimentalAPI
class UserService(
    private val repo: UserRepository,
    private val tokenService: JWTTokenService,
    private val passwordEncoder: PasswordEncoder
) {
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
        // TODO: handle concurrency
        val model = repo.getById(id) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.old, model.password)) {
           throw PasswordChangeException("Wrong password!")
        }
        val copy = model.copy(password = passwordEncoder.encode(input.new))
        repo.save(copy)
    }

    suspend fun authenticate(input: AuthenticationRequestDto): AuthenticationResponseDto {
        val model = repo.getByUsername(input.username) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.password, model.password)) {
            throw InvalidPasswordException("Wrong password!")
        }

        val token = tokenService.generate(model.id)
        return AuthenticationResponseDto(token)
    }

    suspend fun save(username: String, password: String) {
        // TODO: check for existence
        // TODO: handle concurrency
        repo.save(UserModel(username = username, password = passwordEncoder.encode(password)))
        return
    }
}