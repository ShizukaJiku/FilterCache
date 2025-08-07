package cache.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tracks dataset progress: min/max IDs, known count, expected total, and completion.
 *
 * <p>This class maintains metadata about the progress of dataset loading, useful for
 * caching and pagination strategies:</p>
 *
 * <ul>
 *     <li>Tracks the smallest and largest known IDs ({@code minId} / {@code maxId}).</li>
 *     <li>Keeps a counter of known elements ({@code knownCount}).</li>
 *     <li>Stores the expected total number of elements ({@code expectedTotal}).</li>
 *     <li>Computes completion percentage and whether the dataset is considered "complete".</li>
 * </ul>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * DatasetProgress<Long> progress = new DatasetProgress<>();
 * progress.updateExpectedTotal(1000);
 * progress.updateBounds(10L);
 * progress.incrementKnown();
 *
 * if (progress.isComplete()) {
 *     log.info("Dataset fully loaded!");
 * }
 * }</pre>
 *
 * <p>Notes:</p>
 * <ul>
 *     <li>{@link #incrementKnown()} and {@link #decrementKnown()} should reflect changes
 *         in your {@code DatasetStorage}.</li>
 *     <li>{@link #updateBounds(I id)} should be called when new IDs are discovered.</li>
 * </ul>
 *
 * @param <I> type of the identifier, must implement {@link Comparable} for bound checks
 */
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.MODULE)
@AllArgsConstructor(access = AccessLevel.MODULE)
public class DatasetProgress<I extends Comparable<I>> {

  /**
   * Smallest known ID in the dataset (nullable if none discovered yet).
   */
  private I minId;

  /**
   * Largest known ID in the dataset (nullable if none discovered yet).
   */
  private I maxId;

  /**
   * Number of elements currently known (present in cache).
   */
  private int knownCount;

  /**
   * Expected total number of elements, or {@code -1} if unknown.
   */
  private int expectedTotal = -1;

  /**
   * Updates the expected total count.
   * <p>If the total was previously unknown or has changed, logs a warning.</p>
   *
   * @param total new expected total
   */
  public void updateExpectedTotal(int total) {
    if (expectedTotal == -1) {
      expectedTotal = total;
      log.debug("Expected total initialized: {}", total);
    } else if (expectedTotal != total) {
      log.warn("Expected total changed from {} to {}", expectedTotal, total);
      expectedTotal = total;
    }
  }

  /**
   * Updates min and max ID boundaries based on a newly discovered ID.
   *
   * @param id newly discovered ID
   */
  public void updateBounds(I id) {
    if (minId == null || id.compareTo(minId) < 0) {
      minId = id;
      log.trace("Updated minId to {}", minId);
    }
    if (maxId == null || id.compareTo(maxId) > 0) {
      maxId = id;
      log.trace("Updated maxId to {}", maxId);
    }
  }

  /**
   * Increments the known count by 1.
   */
  public void incrementKnown() {
    knownCount++;
    log.trace("Known count incremented to {}", knownCount);
  }

  /**
   * Decrements the known count by 1, if greater than zero.
   */
  public void decrementKnown() {
    if (knownCount > 0) {
      knownCount--;
      log.trace("Known count decremented to {}", knownCount);
    }
  }

  /**
   * Returns the completion percentage (0-100) based on known elements
   * and the expected total.
   *
   * @return completion percentage as an integer
   */
  public int getCompletionPercentage() {
    return expectedTotal <= 0 ? 0 : (int) ((knownCount * 100.0f) / expectedTotal);
  }

  /**
   * Returns true if the dataset is considered complete.
   * <p>A dataset is complete if the expected total is known and
   * the number of known elements is greater than or equal to that total.</p>
   *
   * @return true if dataset is complete
   */
  public boolean isComplete() {
    return expectedTotal != -1 && knownCount >= expectedTotal;
  }

  /**
   * Resets all progress fields to their initial state.
   */
  public void reset() {
    minId = maxId = null;
    knownCount = 0;
    expectedTotal = -1;
    log.debug("DatasetProgress reset to initial state");
  }
}
