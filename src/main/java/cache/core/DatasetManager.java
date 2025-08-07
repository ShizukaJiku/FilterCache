package cache.core;

import cache.pageload.ImmediateAroundPageStrategy;
import cache.pageload.PageLoadStrategy;
import cache.provider.DataProvider;
import cache.provider.DataProviderModeStrategy;
import cache.provider.DataRequest;
import cache.provider.DataResponse;
import cache.provider.impl.DataProviderSimpleMode;
import cache.snapshot.DatasetManagerSnapshot;
import filter.core.DatasetFilter;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@AllArgsConstructor
public abstract class DatasetManager<T extends Identifiable<I>, I extends Comparable<I>> {
  protected final DatasetCacheManager<T, I> datasetRaw;
  protected final Map<String, DatasetFilterManager<I>> mapDatasetFilterManager;
  private final Consumer<T> whenInsert;
  private final PageLoadStrategy pageLoadStrategy;
  private final DataProviderModeStrategy<T, I> dataProviderModeStrategy;

  protected DatasetManager(DatasetCacheManager<T, I> datasetRaw, Map<String, DatasetFilterManager<I>> mapDatasetFilterManager, Consumer<T> whenInsert) {
    this(datasetRaw, mapDatasetFilterManager, whenInsert, new ImmediateAroundPageStrategy(), new DataProviderSimpleMode<>());
  }

  public List<T> getData(
          DatasetFilter<T> datasetFilter,
          int page,
          int pageSize,
          DataProvider<T, I> dataProvider) {

    var filterManager = mapDatasetFilterManager.get(datasetFilter.fingerprint());

    if (!isPageCached(filterManager, page, pageSize)) {
      var dataResponse = dataProviderModeStrategy.getDataOfProvider(
              new DataRequest<>(datasetFilter, page, pageSize),
              dataProvider
      );

      if (filterManager == null) {
        filterManager = new DatasetFilterManager<>(dataResponse.getTotalFiltered());
        mapDatasetFilterManager.put(datasetFilter.fingerprint(), filterManager);
      }

      updateCache(dataResponse, filterManager);
    }

    prefetchPagesIfNeeded(datasetFilter, page, pageSize, filterManager, dataProvider);

    return datasetRaw.getPresentDataOfIdList(filterManager.getIdList(page, pageSize));
  }

  public List<T> getCachedData() {
    return datasetRaw.getPresentData();
  }

  public List<T> findByID(List<I> idList) {
    return datasetRaw.getPresentDataOfIdList(idList);
  }

  private boolean isPageCached(DatasetFilterManager<I> filterManager, int page, int pageSize) {
    return filterManager != null
            && filterManager.isPageFullyCached(page, pageSize)
            && datasetRaw.hasEnoughData(filterManager.getIdList(page, pageSize));
  }

  private void prefetchPagesIfNeeded(
          DatasetFilter<T> datasetFilter,
          int currentPage,
          int pageSize,
          DatasetFilterManager<I> filterManager,
          DataProvider<T, I> dataProvider) {

    var pagesAlreadyCached = filterManager.getPagesAlreadyCached(pageSize);
    int totalPages = Math.max((int) Math.ceil((double) filterManager.getTotalElements() / pageSize), 1);

    if (pagesAlreadyCached.size() >= totalPages) return;

    var pagesToFetch = pageLoadStrategy.pagesToFetch(currentPage, pagesAlreadyCached, totalPages);
    if (pagesToFetch.isEmpty()) return;

    dataProviderModeStrategy.fetchData(
            new DataRequest<>(datasetFilter, pagesToFetch, pageSize),
            dataProvider,
            dataResponse -> updateCache(dataResponse, filterManager)
    );
  }

  private void updateCache(
          DataResponse<T, I> dataResponse,
          DatasetFilterManager<I> filterManager) {

    filterManager.updateData(
            dataResponse.getDataID(),
            dataResponse.getPage(),
            dataResponse.getPageSize()
    );

    datasetRaw.updateFromPage(
            dataResponse.getData(),
            dataResponse.getTotalDataset(),
            whenInsert
    );
  }

  public abstract DatasetManagerSnapshot<T, I> toSnapshot();
}
