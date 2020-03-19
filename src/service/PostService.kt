package service

import dto.posts.Post
import io.ktor.features.NotFoundException
import io.ktor.util.KtorExperimentalAPI
import model.PostModel
import repository.PostRepository

@KtorExperimentalAPI
class PostService(private val repo: PostRepository) {
    suspend fun getAll(): List<Post> {
        return repo.getAll().map { it.getProperPostObject() }
    }

    suspend fun getById(id: Long): Post {
        val model = repo.getById(id) ?: throw NotFoundException()
        return model.getProperPostObject()
    }

    suspend fun create(input: PostModel): Post {
        return repo.create(input).getProperPostObject()
    }

    suspend fun update(id: Long, input: PostModel): Post {
        return repo.update(id, input).getProperPostObject()
    }

    suspend fun removeById(id: Long) {
        repo.removeById(id)
    }

    suspend fun likeById(id: Long): Post? {
        return repo.likeById(id)?.getProperPostObject()
    }

    suspend fun dislikeById(id: Long): Post? {
        return repo.dislikeById(id)?.getProperPostObject()
    }

    suspend fun repost(input: PostModel): Post? {
        return repo.repost(input)?.getProperPostObject()
    }

    suspend fun shareById(id: Long): Post? {
        return repo.shareById(id)?.getProperPostObject()
    }
}