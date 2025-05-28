package org.technoserve.farmcollector.database.helpers

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.viewmodels.FarmViewModel

/**
 *
 * This class is used to implement a PagingSource for the CollectionSite entity
 */
class MyPagingSource(
    private val farmViewModel: FarmViewModel
) : PagingSource<Int, CollectionSite>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CollectionSite> {
        return try {
            val currentPage = params.key ?: 1
            // Use withContext to ensure this operation runs on IO dispatcher
            val response = withContext(Dispatchers.IO) {
                farmViewModel.getCollectionSites(currentPage, params.loadSize)
            }
            val endOfPaginationReached = response.isEmpty()

            LoadResult.Page(
                data = response,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (endOfPaginationReached) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CollectionSite>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}



