package cache.pageload;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Strategy that fetches pages around the requested page.
 * <p>
 * - Pages are 1-based.
 * - Ensures only missing pages are returned.
 * - Fully defensive against nulls and invalid ranges.
 */
public class AroundRequestedPageStrategy implements PageLoadStrategy {

  private final int rangeBefore;
  private final int rangeAfter;

  /**
   * Creates a strategy that fetches pages around the requested page.
   *
   * @param rangeBefore number of pages to fetch before the requested page (≥0)
   * @param rangeAfter  number of pages to fetch after the requested page (≥0)
   */
  public AroundRequestedPageStrategy(int rangeBefore, int rangeAfter) {
    if (rangeBefore < 0 || rangeAfter < 0) {
      throw new IllegalArgumentException("Page ranges must be >= 0");
    }
    this.rangeBefore = rangeBefore;
    this.rangeAfter = rangeAfter;
  }

  private static void validateInputs(
          Integer requestedPage,
          Set<Integer> pagesAlreadyCached,
          Integer totalPages) {

    if (requestedPage == null || totalPages == null || pagesAlreadyCached == null) {
      throw new IllegalArgumentException(
              "requestedPage, totalPages, and pagesAlreadyCached cannot be null");
    }
    if (requestedPage < 1) {
      throw new IllegalArgumentException("requestedPage must be >= 1");
    }
    if (totalPages < 1) {
      throw new IllegalArgumentException("totalPages must be >= 1");
    }
    for (Integer page : pagesAlreadyCached) {
      if (page == null || page < 1 || page > totalPages) {
        throw new IllegalArgumentException(
                "pagesAlreadyCached contains invalid value: " + page);
      }
    }
  }

  @Override
  public NavigableSet<Integer> pagesToFetch(
          Integer requestedPage,
          Set<Integer> pagesAlreadyCached,
          Integer totalPages) {

    validateInputs(requestedPage, pagesAlreadyCached, totalPages);

    NavigableSet<Integer> pagesToLoad = new TreeSet<>();
    int start = Math.max(1, requestedPage - rangeBefore);
    int end = Math.min(totalPages, requestedPage + rangeAfter);

    for (int i = start; i <= end; i++) {
      if (!pagesAlreadyCached.contains(i)) {
        pagesToLoad.add(i);
      }
    }

    return Collections.unmodifiableNavigableSet(pagesToLoad);
  }
}