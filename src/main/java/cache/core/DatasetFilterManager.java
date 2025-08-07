package cache.core;

import cache.snapshot.DatasetFilterManagerSnapshot;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * <p>
 * Manages a paginated, filtered collection of dataset IDs.  It tracks which positions
 * have been populated (cached) and provides efficient lookup of fully cached pages.
 * </p>
 *
 * <h3>Key features:</h3>
 * <ul>
 *   <li>Fixed-size storage of IDs (positions initially empty).</li>
 *   <li>Fast marking of individual positions and pages via {@link BitSet}.</li>
 *   <li>Immutable snapshots of page sublists for safe external exposure.</li>
 *   <li>Progress tracking via a known-count counter.</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This class is <em>not</em> thread-safe.  If you intend to share
 * instances across threads, you must provide external synchronization.</p>
 *
 * @param <I> the type of the dataset identifier; must implement {@link Comparable}
 */
@AllArgsConstructor
public class DatasetFilterManager<I extends Comparable<I>> {

  /**
   * Internal storage of IDs.  Positions are initially unpopulated; once a position
   * is updated, its ID goes here.
   */
  private final List<I> idStorage;

  /**
   * A BitSet marking which positions in {@link #idStorage} have been populated.
   * A bit = 1 indicates the corresponding index holds a valid ID.
   */
  private final BitSet populatedPositions;

  /**
   * A BitSet marking which pages are fully populated.  A bit = 1 at pageIndex
   * indicates that all positions within that page have been populated.
   * Page indices are 1-based in the public API but stored 0-based here.
   */
  private final BitSet populatedPages;

  /**
   * Total number of elements in the filtered dataset.  Never negative.
   */
  @Getter
  private final int totalElements;

  /**
   * Count of how many positions have actually been populated so far.
   * Used for progress metrics.
   */
  @Getter
  private int knownCount;

  /**
   * Constructs a new manager for a filtered dataset of the given size.
   *
   * @param totalElements the total number of filtered items; must be &ge; 0
   * @throws IllegalArgumentException if {@code totalElements} is negative
   */
  public DatasetFilterManager(int totalElements) {
    if (totalElements < 0) {
      throw new IllegalArgumentException("totalElements must be non-negative");
    }
    this.totalElements = totalElements;
    this.idStorage = new ArrayList<>(Collections.nCopies(totalElements, null));
    this.populatedPositions = new BitSet(totalElements);
    this.populatedPages = new BitSet();
    this.knownCount = 0;
  }

  public static <I extends Comparable<I>> DatasetFilterManager<I> fromSnapshot(DatasetFilterManagerSnapshot<I> snapshot) {
    return new DatasetFilterManager<>(snapshot.getIdStorage(), snapshot.getPopulatedPositions(), snapshot.getPopulatedPages(), snapshot.getTotalElements(), snapshot.getKnownCount());
  }

  /**
   * Returns an immutable list of IDs for the specified page.
   * Positions that are not yet populated will be {@code null} in the returned list.
   *
   * @param page     1-based page index (&ge; 1)
   * @param pageSize number of items per page (&ge; 1)
   * @return an immutable sublist of length up to {@code pageSize}, or
   * an empty list if {@code page} is out of range
   */
  public List<I> getIdList(int page, int pageSize) {
    int fromIndex = (page - 1) * pageSize;
    if (page < 1 || fromIndex >= totalElements) {
      return List.of();
    }
    int toIndex = Math.min(page * pageSize, totalElements);
    // Return an immutable snapshot
    return List.copyOf(idStorage.subList(fromIndex, toIndex));
  }

  /**
   * Updates a page of IDs.  For each position within the page that is non-null,
   * marks the position as populated and increments {@link #knownCount} if it
   * was previously empty.  If every position in the page is populated after this
   * update, the page is marked fully populated.
   *
   * @param newIdList the list of new IDs for this page; may contain nulls for positions
   * @param page      1-based page index (&ge; 1)
   * @param pageSize  number of items per page (&ge; 1)
   * @throws IllegalArgumentException if {@code page} &lt; 1
   */
  public void updateData(List<I> newIdList, int page, int pageSize) {
    if (page < 1) {
      throw new IllegalArgumentException("page index must be >= 1");
    }
    if (newIdList == null || newIdList.isEmpty()) {
      return;
    }
    int fromIndex = (page - 1) * pageSize;
    if (fromIndex >= totalElements) {
      return;
    }
    int toIndex = Math.min(page * pageSize, totalElements);
    int writeCount = Math.min(newIdList.size(), toIndex - fromIndex);

    boolean allPopulated = true;
    for (int i = 0; i < writeCount; i++) {
      I newId = newIdList.get(i);
      int storageIndex = fromIndex + i;
      if (newId == null) {
        allPopulated = false;
        continue;
      }
      if (!populatedPositions.get(storageIndex)) {
        populatedPositions.set(storageIndex);
        knownCount++;
        idStorage.set(storageIndex, newId);
      } else {
        idStorage.set(storageIndex, newId);
      }
    }

    if (allPopulated && writeCount == (toIndex - fromIndex)) {
      populatedPages.set(page - 1);
    }
  }

  /**
   * Returns the set of 1-based page indices that are fully populated.
   *
   * @return a set of page numbers for which every position is known
   */
  public Set<Integer> getPagesAlreadyCached(int pageSize) {
    Set<Integer> result = new HashSet<>();
    int bit = populatedPages.nextSetBit(0);
    while (bit >= 0) {
      result.add(bit + 1);
      bit = populatedPages.nextSetBit(bit + 1);
    }
    return result;
  }

  /**
   * Returns an unmodifiable live view of internal storage.
   * <p><strong>Warning:</strong> modifying this list via reflection or other
   * hidden means will corrupt internal state.  Use with care.</p>
   *
   * @return unmodifiable list of length {@code totalElements}
   */
  public List<I> getAllIds() {
    return Collections.unmodifiableList(idStorage);
  }

  /**
   * Checks if a page is fully populated (no missing IDs).
   *
   * @param page     1-based page index
   * @param pageSize page size
   * @return true if the page is fully cached
   */
  public boolean isPageFullyCached(int page, int pageSize) {
    int fromIndex = (page - 1) * pageSize;
    if (fromIndex >= totalElements || page < 1) return false;

    int toIndex = Math.min(page * pageSize, totalElements);

    for (int i = fromIndex; i < toIndex; i++) {
      if (!populatedPositions.get(i)) return false;
    }
    return true;
  }

  public DatasetFilterManagerSnapshot<I> toSnapshot() {
    return new DatasetFilterManagerSnapshot<>(
            totalElements,
            knownCount,
            List.copyOf(idStorage),
            (BitSet) populatedPositions.clone(),
            (BitSet) populatedPages.clone()
    );
  }
}
