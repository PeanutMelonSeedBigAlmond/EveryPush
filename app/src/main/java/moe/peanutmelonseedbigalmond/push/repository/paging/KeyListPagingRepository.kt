package moe.peanutmelonseedbigalmond.push.repository.paging

import moe.peanutmelonseedbigalmond.push.bean.KeyInfo
import moe.peanutmelonseedbigalmond.push.network.Client

class KeyListPagingRepository : IndexSizeBasedPagingRepository<KeyInfo>() {
    override suspend fun loadData(pageIndex: Int, pageSize: Int): List<KeyInfo> {
        return Client.listKey(pageIndex, pageSize).map {
            return@map KeyInfo(it.id, it.name, it.key, it.createdAt, it.uid)
        }
    }
}