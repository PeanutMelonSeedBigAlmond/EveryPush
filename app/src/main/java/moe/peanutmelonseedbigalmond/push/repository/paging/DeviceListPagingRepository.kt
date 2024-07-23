package moe.peanutmelonseedbigalmond.push.repository.paging

import moe.peanutmelonseedbigalmond.push.bean.DeviceInfo
import moe.peanutmelonseedbigalmond.push.network.Client

class DeviceListPagingRepository : IndexSizeBasedPagingRepository<DeviceInfo>() {
    override suspend fun loadData(pageIndex: Int, pageSize: Int): List<DeviceInfo> {
        val response = Client.listDevices(pageIndex, pageSize)
        val data = response.map {
            return@map DeviceInfo(it.id, it.uid, it.name, it.platform, it.deviceToken)
        }
        return data
    }
}