package route

import dto.users.AuthenticationRequestDto
import dto.users.RegistrationRequestDto
import dto.users.UserResponseDto
import io.ktor.application.call
import io.ktor.auth.ForbiddenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.features.BadRequestException
import io.ktor.features.NotFoundException
import io.ktor.features.ParameterConversionException
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import model.PostModel
import model.UserModel
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import repository.PostRepository
import service.FileService
import service.PostService
import service.UserService

class RoutingV1 @KtorExperimentalAPI constructor(
    private val staticPath: String,
    private val postService: PostService,
    private val fileService: FileService,
    private val userService: UserService
) {
    @KtorExperimentalAPI
    fun setup(configuration: Routing) {
        with(configuration) {
            route("/api/v1") {
                route("/") {
                    post("/registration") {
                        val input = call.receive<RegistrationRequestDto>()
                        val response = userService.register(input)
                        call.respond(response)
                    }
                    post("/authentication") {
                        val input = call.receive<AuthenticationRequestDto>()
                        val response = userService.authenticate(input)
                        call.respond(response)
                    }
                    authenticate("jwt") {
                        route("/me") {
                            get {
                                val me = call.authentication.principal<UserModel>()
                                call.respond(UserResponseDto.fromModel(me!!))
                            }
                        }
                        route("/media") {
                            post {
                                val multipart = call.receiveMultipart()
                                val response = fileService.save(multipart)
                                call.respond(response)
                            }
                        }
                        route("/posts") {
                            val repository by kodein().instance<PostRepository>()
                            get {
                                //Скачать все посты
                                val response = postService.getAll()
                                call.respond(response)
                            }
                            get("/{id}") {
                                //Скачать пост с данным id
                                val id =
                                    call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                        "id",
                                        "Long"
                                    )
                                val response = postService.getById(id)
                                call.respond(response)
                            }
                            post("/create") {
                                //Создание нового поста

                                val inputPostModel = call.receive<PostModel>()
                                val response = postService.create(inputPostModel)
                                call.respond(response)
                            }
                            patch("/{id}/update") {
                                val me =
                                    call.authentication.principal<UserModel>() ?: throw ParameterConversionException(
                                        "me",
                                        "UserModel"
                                    )
                                val id =
                                    call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                        "id",
                                        "Long"
                                    )
                                if (postService.getById(id).author == me.username) {
                                    val inputPostModel = call.receive<PostModel>()
                                    val response = postService.update(id, inputPostModel)
                                    call.respond(response)
                                } else {
                                    //call.respond(ForbiddenResponse())
                                    throw Exception("Редактировать посты может только автор поста!")
                                }
                            }
                            delete("/{id}") {
                                val me =
                                    call.authentication.principal<UserModel>() ?: throw ParameterConversionException(
                                        "me",
                                        "UserModel"
                                    )
                                val id =
                                    call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                        "id",
                                        "Long"
                                    )
                                if (postService.getById(id).author == me.username) {
                                    if (postService.removeById(id)) call.respond(HttpStatusCode.OK) else call.respond(
                                        HttpStatusCode.NotModified
                                    )
                                } else {
                                    //call.respond(ForbiddenResponse())
                                    throw Exception("Удалять посты может только автор поста!")
                                }
                            }
                            post("/{id}/likes") {
                                val id =
                                    call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                        "id",
                                        "Long"
                                    )
                                val response = postService.likeById(id)
                                response?.let { call.respond(it) }
                            }
                            delete("/{id}/likes") {
                                val id =
                                    call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                        "id",
                                        "Long"
                                    )
                                val response = postService.dislikeById(id)
                                response?.let { call.respond(it) }
                            }
                            post("/repost") {
                                val inputPostModel = call.receive<PostModel>()
                                val response = postService.repost(inputPostModel)
                                response?.let { call.respond(it) }
                            }
                            post("/{id}/share") {
                                val id =
                                    call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                        "id",
                                        "Long"
                                    )
                                val response = postService.shareById(id)
                                response?.let { call.respond(it) }
                            }
                        }
                    }
                    // Static feature. Try to access `/static/ktor_logo.svg`
                    static("/static") {
                        files(staticPath)
                    }
                }
            }
        }
    }
}