package moe.peanutmelonseedbigalmond.push.datasource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.network.response.DeviceResponse

class DevicesDataSource(
    private val client: Client,
) : PagingSource<String, DeviceResponse.DeviceItem>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, DeviceResponse.DeviceItem> {
        return try {
            delay(3000)
            val cursor = params.key
            Log.i("TAG", "load: Start loading key=$cursor")
            val requestData = client.listDevices(20, cursor)
            val nextCursor = if (requestData.pageInfo.hasNextPage) {
                requestData.pageInfo.lastCursor!!
            } else {
                Log.i("TAG", "load: Load end")
                null
            }
            LoadResult.Page(data = requestData.items, prevKey = null, nextKey = nextCursor)
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, DeviceResponse.DeviceItem>): String? = null
}