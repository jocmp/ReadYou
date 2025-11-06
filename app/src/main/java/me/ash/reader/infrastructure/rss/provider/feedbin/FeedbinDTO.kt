package me.ash.reader.infrastructure.rss.provider.feedbin

import com.google.gson.annotations.SerializedName

object FeedbinDTO {
    data class Subscription(
        val id: Long,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("feed_id") val feedId: Long,
        val title: String,
        @SerializedName("feed_url") val feedUrl: String,
        @SerializedName("site_url") val siteUrl: String
    )

    data class Icon(
        val host: String,
        val url: String,
    )

    data class Tagging(
        val id: Long,
        @SerializedName("feed_id") val feedId: Long,
        val name: String
    )

    data class SavedSearch(
        val id: Long,
        val name: String,
        val query: String,
    )

    data class Entry(
        val id: Long,
        @SerializedName("feed_id") val feedId: Long,
        val title: String?,
        val url: String?,
        @SerializedName("extracted_content_url") val extractedContentUrl: String?,
        val author: String?,
        val content: String?,
        val summary: String?,
        val published: String,
        @SerializedName("created_at") val createdAt: String,
        val images: Images? = null,
        val enclosure: Enclosure? = null,
    ) {
        data class Images(
            @SerializedName("original_url") val originalUrl: String,
            @SerializedName("size_1") val size1: SizeOne,
        ) {
            data class SizeOne(
                @SerializedName("cdn_url") val cdnUrl: String,
            )
        }
    }

    data class Enclosure(
        @SerializedName("enclosure_url") val enclosureUrl: String,
        @SerializedName("enclosure_type") val enclosureType: String?,
        @SerializedName("enclosure_length") val enclosureLength: String?,
        @SerializedName("itunes_duration") val itunesDuration: String?,
        @SerializedName("itunes_image") val itunesImage: String?,
    )
}
