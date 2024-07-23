package moe.peanutmelonseedbigalmond.push.repository.paging

import moe.peanutmelonseedbigalmond.push.bean.MessageGroup
import moe.peanutmelonseedbigalmond.push.network.Client

class MessageGroupListPagingRepository : IndexSizeBasedPagingRepository<MessageGroup>() {
    override suspend fun loadData(pageIndex: Int, pageSize: Int): List<MessageGroup> {
        return Client.listMessageGroup(pageIndex, pageSize).map {
            return@map MessageGroup(it.id, it.uid, it.groupId, it.name, it.createdAt)
        }
    }
}