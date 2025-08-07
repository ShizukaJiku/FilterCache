package cache.provider;

import cache.core.Identifiable;

/**
 * Provides paginated access to a dataset, optionally applying filtering logic.
 *
 * <p>The DataProvider is responsible for fetching a specific page of results
 * from the underlying data source, which may be a database, API, or in-memory
 * dataset.
 *
 * <p>Implementations must:
 * <ul>
 *   <li>Return an empty {@link DataResponse#getData()} if the page is out of range.</li>
 *   <li>Never return {@code null} as a {@link DataResponse}.</li>
 *   <li>Handle 1-based page indexing (page 1 is the first page).</li>
 * </ul>
 *
 * @param <T>  the dataset entity type
 * @param <ID> the type of the entity identifier
 */
public interface DataProvider<T extends Identifiable<ID>, ID extends Comparable<ID>> {

  /**
   * Requests a page of data from the provider.
   *
   * @param dataRequest contains the filter and page size information
   * @param page        the 1-based page index to fetch
   * @return a non-null {@link DataResponse} containing the requested data
   * @throws IllegalArgumentException if {@code dataRequest} is null
   *                                  or {@code page} is less than 1
   */
  DataResponse<T, ID> requestData(DataRequest<T, ID> dataRequest, Integer page);
}
