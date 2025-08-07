package cache.pageload;

import java.util.NavigableSet;
import java.util.Set;

/**
 * Defines a strategy for determining which pages should be fetched
 * from the data provider based on the requested page and the current
 * cache state.
 *
 * <p>Implementations can define different prefetching behaviors,
 * for example:
 * <ul>
 *   <li>Fetch only the requested page.</li>
 *   <li>Fetch the requested page plus surrounding pages.</li>
 *   <li>Implement predictive or lazy prefetch strategies.</li>
 * </ul>
 *
 * <p>Page indexes are expected to be 1-based.
 */
public interface PageLoadStrategy {

  /**
   * Determines which pages should be fetched based on the current request
   * and the pages already cached.
   *
   * @param requestedPage      the 1-based index of the requested page (never null)
   * @param pagesAlreadyCached set of pages already cached (cannot be null, no null entries)
   * @param totalPages         total number of pages in the dataset (never null, ≥ 1)
   * @return a navigable set of pages to fetch, in ascending order, never null.
   */
  NavigableSet<Integer> pagesToFetch(
          Integer requestedPage,
          Set<Integer> pagesAlreadyCached,
          Integer totalPages
  );
}
