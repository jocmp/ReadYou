package me.ash.reader.domain.model.feedgroup

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.group.Group

/**
 * Junction table to handle feeds that belong to one or more groups
 */
@Entity(
    tableName = "feed_group",
    primaryKeys = ["feedId", "groupId", "accountId"],
    foreignKeys = [
        ForeignKey(
            entity = Feed::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
data class FeedGroup(
    @ColumnInfo(index = true)
    val feedId: String,
    @ColumnInfo(index = true)
    val groupId: String,
    @ColumnInfo(index = true)
    val accountId: Int,
)
