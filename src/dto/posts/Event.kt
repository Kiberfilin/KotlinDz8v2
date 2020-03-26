package dto.posts

import dto.posts.posttypes.PostType

class Event(
    id: Long,
    author: String,
    content: String,
    created: String,
    likeCount: Long,
    commentCount: Long,
    shareCount: Long,
    likedByMe: Boolean,
    commentedByMe: Boolean,
    sharedByMe: Boolean,
    postType: PostType,
    source: Post? = null,
    attachments: ArrayList<Attachment>? = null,
    var address: String?,
    var coordinates: Coordinates? = Coordinates()
) : Post(
    id,
    author,
    content,
    created,
    likeCount,
    commentCount,
    shareCount,
    likedByMe,
    commentedByMe,
    sharedByMe,
    postType,
    source,
    attachments
)

data class Coordinates(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)