package moe.peanutmelonseedbigalmond.push.repository.paging

import moe.peanutmelonseedbigalmond.push.bean.MessageExcerptInfo
import moe.peanutmelonseedbigalmond.push.network.Client

class AllMessageListPagingRepository: IndexSizeBasedPagingRepository<MessageExcerptInfo>() {
    override suspend fun loadData(pageIndex: Int, pageSize: Int): List<MessageExcerptInfo> {
        return Client.listAllMessage(pageIndex, pageSize).map {
            return@map MessageExcerptInfo(
                it.id,
                it.uid,
                it.title,
                it.excerpt,
                it.type,
                it.coverUrl,
                it.encrypted,
                it.messageGroupId,
                it.pushedAt
            )
        }
    }
}