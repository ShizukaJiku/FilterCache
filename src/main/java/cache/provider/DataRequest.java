package cache.provider;

import cache.core.Identifiable;
import filter.core.DatasetFilter;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a paginated data request with an optional filter.
 *
 * <p>This is the input object used by {@link DataProvider} to fetch
 * filtered pages from a dataset.
 *
 * <p>Features:
 * <ul>
 *   <li>Supports 1-based page indexing</li>
 *   <li>Can request multiple pages at once (for predictive caching)</li>
 *   <li>Immutable and safe to use as a cache key if implementing equals/hashCode</li>
 * </ul>
 *
 * @param <T> the dataset entity type
 * @param <I> the type of the entity identifier
 */
@Getter
public class DataRequest<T extends Identifiable<I>, I extends Comparable<I>> {

  /**
   * The filter to apply (never null).
   */
  private final DatasetFilter<T> datasetFilter;
  /**
   * The 1-based page indices to fetch.
   */
  private final Set<Integer> pages;
  /**
   * Number of items per page (must be > 0).
   */
  private final int pageSize;

  /**
   * Creates a request for a single page.
   */
  public DataRequest(DatasetFilter<T> datasetFilter, int page, int pageSize) {
    this(datasetFilter, Set.of(page), pageSize);
  }

  /**
   * Creates a request for multiple pages.
   */
  public DataRequest(DatasetFilter<T> datasetFilter, Set<Integer> pages, int pageSize) {
    if (datasetFilter == null) throw new IllegalArgumentException("DatasetFilter must not be null");
    if (pages == null || pages.isEmpty()) throw new IllegalArgumentException("Pages must not be null or empty");
    if (pageSize <= 0) throw new IllegalArgumentException("Page size must be > 0");
    for (Integer p : pages) {
      if (p == null || p < 1) throw new IllegalArgumentException("Page numbers must be >= 1");
    }

    this.datasetFilter = datasetFilter;
    this.pages = Collections.unmodifiableSet(pages);
    this.pageSize = pageSize;
  }
}
