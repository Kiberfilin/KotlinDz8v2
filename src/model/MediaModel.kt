package model

import dto.posts.Attachment

enum class MediaType {
    IMAGE
}

data class MediaModel(val id: String, val mediaType: MediaType) : Attachment