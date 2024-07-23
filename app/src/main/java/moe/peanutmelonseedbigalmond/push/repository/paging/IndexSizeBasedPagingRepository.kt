package moe.peanutmelonseedbigalmond.push.repository.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState

abstract class IndexSizeBasedPagingRepository<Value : Any> : PagingSource<Int, Value>() {
    final override fun getRefreshKey(state: PagingState<Int, Value>): Int? = null

    final override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
        val pageIndex = params.key ?: 0
        val pageSize = params.loadSize
        Log.i(this::class.qualifiedName, "loading pageIndex = $pageIndex")
        try {
            val response = loadData(pageIndex, pageSize)
            return LoadResult.Page(
                data = response,
                prevKey = if (pageIndex == 0) null else pageIndex - 1,
                nextKey = if (response.isEmpty()) null else pageIndex + 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }

    protected abstract suspend fun loadData(pageIndex: Int, pageSize: Int): List<Value>
}