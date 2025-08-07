package cache.provider;

import cache.core.Identifiable;

/**
 * Defines how data should be retrieved from a {@link DataProvider},
 * supporting both single-page and multi-page fetching modes.
 *
 * <p>Implementations can define different strategies:
 * <ul>
 *   <li>Sequential fetching (simple mode)</li>
 *   <li>Parallel or async fetching for predictive caching</li>
 *   <li>Streaming modes for large datasets</li>
 * </ul>
 *
 * @param <T> the dataset entity type
 * @param <I> the type of the entity identifier
 */
public interface DataProviderModeStrategy<T extends Identifiable<I>, I extends Comparable<I>> {

  /**
   * Fetches a single page of data synchronously.
   *
   * @param dataRequest  the request containing filter and pagination
   * @param dataProvider the underlying data provider
   * @return a single-page data response
   */
  DataResponse<T, I> getDataOfProvider(
          DataRequest<T, I> dataRequest,
          DataProvider<T, I> dataProvider);

  /**
   * Fetches one or more pages and passes each response to the provided consumer.
   * <p>Implementations may perform the requests sequentially or in parallel.
   *
   * @param dataRequest      the request containing filter and page set
   * @param dataProvider     the underlying data provider
   * @param responseConsumer callback invoked for each received page
   */
  void fetchData(
          DataRequest<T, I> dataRequest,
          DataProvider<T, I> dataProvider,
          java.util.function.Consumer<DataResponse<T, I>> responseConsumer);
}
