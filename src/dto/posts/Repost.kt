package dto.posts

import dto.posts.posttypes.PostType

class Repost(
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
    source: Post? = null
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
    source
)