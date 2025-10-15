package me.ash.reader.domain.service

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result.success
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import me.ash.reader.R
import me.ash.reader.domain.data.SyncLogger
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.account.security.FeedbinSecurityKey
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.infrastructure.rss.provider.feedbin.FeedbinAPI
import me.ash.reader.ui.ext.decodeHTML
import me.ash.reader.ui.ext.spacerDollar
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.get

class FeedbinRssService
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val rssHelper: RssHelper,
    private val notificationHelper: NotificationHelper,
    private val groupDao: GroupDao,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val workManager: WorkManager,
    private val accountService: AccountService,
    private val syncLogger: SyncLogger,
) : AbstractRssRepository(
    articleDao,
    groupDao,
    feedDao,
    workManager,
    rssHelper,
    notificationHelper,
    ioDispatcher,
    defaultDispatcher,
    accountService,
) {
    override val importSubscription: Boolean = false
    override val addSubscription: Boolean = true
    override val moveSubscription: Boolean = true
    override val deleteSubscription: Boolean = true
    override val updateSubscription: Boolean = true

    override suspend fun validCredentials(account: Account): Boolean {
        return feedbin.validCredentials()
    }

    override suspend fun clearAuthorization() {
        FeedbinAPI.clearInstance()
    }

    private val feedbin
        get() =
            FeedbinSecurityKey(accountService.getCurrentAccount().securityKey).run {
                FeedbinAPI.getInstance(
                    context = context,
                    username = username!!,
                    password = password!!,
                    syncLogger = syncLogger,
                )
            }

    override suspend fun sync(
        accountId: Int,
        feedId: String?,
        groupId: String?
    ): ListenableWorker.Result {
        val account = accountService.getAccountById(accountId)
        requireNotNull(account) { "cannot find account" }
        check(account.type.id == AccountType.Feedbin.id) { "account type is invalid" }

        syncFeeds(accountId)

        return success()
    }

    private suspend fun syncFeeds(accountId: Int) {
//        feedbin.icons()
        val feeds = feedbin.subscriptions().map {

//
//            Feed(
//                id = accountId.spacerDollar(),
//                name = it.title,
//                url = it.feedUrl,
//                groupId = accountId.spacerDollar(feedsGroupsMap[it.id.toString()]!!),
//                accountId = accountId,
//                icon = faviconsById[it.favicon_id]?.data,
//            )
        }
//
//        feedDao.insertOrUpdate(
//            feedsBody.feeds?.map {
//                Feed(
//                    id = accountId.spacerDollar(it.id!!),
//                    name = it.title.decodeHTML() ?: context.getString(R.string.empty),
//                    url = it.url!!,
//                    groupId = accountId.spacerDollar(feedsGroupsMap[it.id.toString()]!!),
//                    accountId = accountId,
//                    icon = faviconsById[it.favicon_id]?.data,
//                )
//            } ?: emptyList()
//        )

    }
}
