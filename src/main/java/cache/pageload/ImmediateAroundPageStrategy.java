package cache.pageload;

/**
 * Page loading strategy that fetches the requested page plus
 * one page before and one page after it.
 *
 * <p>This is a simple form of prefetching that reduces the likelihood
 * of missing adjacent pages during sequential navigation.
 *
 * <p>Equivalent to {@code new AroundRequestedPageStrategy(1, 1)}.
 */
public final class ImmediateAroundPageStrategy extends AroundRequestedPageStrategy {

  /**
   * Creates a strategy that prefetches the requested page and
   * its immediate neighbors (previous and next page).
   */
  public ImmediateAroundPageStrategy() {
    super(1, 1);
  }
}
