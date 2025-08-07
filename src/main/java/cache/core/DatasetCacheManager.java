package cache.core;

import cache.snapshot.DatasetCacheSnapshot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * High-level manager for caching dataset elements.
 *
 * <p>This class orchestrates two core components:</p>
 * <ul>
 *   <li>{@link DatasetStorage}: stores present elements and tracks missing IDs (gaps).</li>
 *   <li>{@link DatasetProgress}: tracks min/max IDs, known count, and completion status.</li>
 * </ul>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Incremental cache updates from paginated providers.</li>
 *   <li>Snapshot creation for external persistence (e.g., Redis or database).</li>
 *   <li>Simple retrieval API for present data and presence checks.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * DatasetCacheManager<User, Long> manager = new DatasetCacheManager<>();
 *
 * // Update cache with a new page
 * manager.updateFromPage(usersPage, totalUsers, user -> log.info("Inserted {}", user));
 *
 * // Check if specific IDs are cached
 * boolean ready = manager.hasEnoughData(List.of(1L, 2L, 3L));
 *
 * // Get a serializable snapshot for Redis
 * DatasetCacheSnapshot<User, Long> snapshot = manager.toSnapshot();
 * }</pre>
 *
 * @param <T> dataset entity type (must implement {@link Identifiable})
 * @param <I> ID type (must implement {@link Comparable})
 */
@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DatasetCacheManager<T extends Identifiable<I>, I extends Comparable<I>> {

  /**
   * Storage of present elements and known-empty IDs.
   */
  private DatasetStorage<T, I> storage = new DatasetStorage<>();

  /**
   * Progress tracker for dataset completion and bounds.
   */
  private DatasetProgress<I> progress = new DatasetProgress<>();

  /**
   * Restores a cache manager from a previously saved snapshot.
   *
   * @param snapshot snapshot to restore from (must not be null)
   * @return new cache manager initialized with the snapshot data
   */
  public static <T extends Identifiable<I>, I extends Comparable<I>> DatasetCacheManager<T, I> fromSnapshot(
          DatasetCacheSnapshot<T, I> snapshot) {
    if (snapshot == null) throw new IllegalArgumentException("Snapshot must not be null");

    // Restore dataset storage
    DatasetStorage<T, I> datasetStorage = new DatasetStorage<>(
            new TreeMap<>(snapshot.getDatasetMap()),
            new TreeSet<>(snapshot.getEmptyIds())
    );

    // Restore progress info
    DatasetProgress<I> datasetProgress = new DatasetProgress<>(
            snapshot.getMinId(),
            snapshot.getMaxId(),
            snapshot.getKnownCount(),
            snapshot.getExpectedTotal()
    );

    log.debug("DatasetCacheManager restored from snapshot: {} items, {} known",
            snapshot.getDatasetMap().size(), snapshot.getKnownCount());

    return new DatasetCacheManager<>(datasetStorage, datasetProgress);
  }

  /**
   * Updates the cache with a new page of items.
   *
   * <p>For each new item:</p>
   * <ul>
   *   <li>Inserts it into storage if not already present.</li>
   *   <li>Updates the known count and min/max IDs.</li>
   *   <li>Invokes the optional {@code whenInsert} callback.</li>
   * </ul>
   *
   * @param items      list of dataset items (null or empty is ignored)
   * @param total      expected total number of elements (used for completion tracking)
   * @param whenInsert optional consumer invoked for each newly inserted item
   */
  public void updateFromPage(List<T> items, int total, Consumer<T> whenInsert) {
    if (items == null || items.isEmpty()) return;

    progress.updateExpectedTotal(total);

    for (T item : items) {
      I id = item.getId();
      if (!storage.contains(id)) {
        storage.put(item);
        progress.incrementKnown();
        progress.updateBounds(id);

        if (whenInsert != null) {
          whenInsert.accept(item);
        }
      }
    }

    log.debug("Cache updated. Known count={}, completion={}%",
            progress.getKnownCount(), progress.getCompletionPercentage());
  }

  /**
   * Checks if all provided IDs are present in the cache.
   *
   * @param idList IDs to check
   * @return true if all IDs are present
   */
  public boolean hasEnoughData(Collection<I> idList) {
    return idList.stream().allMatch(storage::contains);
  }

  /**
   * Retrieves present items for the given list of IDs.
   * <p>Missing or null IDs are skipped.</p>
   *
   * @param idList collection of IDs
   * @return list of present items (may be empty, never null)
   */
  public List<T> getPresentDataOfIdList(Collection<I> idList) {
    return storage.getValues(idList);
  }

  /**
   * Returns all present items in the cache.
   *
   * @return immutable list of present items
   */
  public List<T> getPresentData() {
    return storage.getAllValues();
  }

  /**
   * Clears all data in this cache manager.
   * <p>Resets both storage and progress.</p>
   */
  public void clear() {
    int before = storage.size();
    storage.clear();
    progress.reset();
    log.debug("Cache cleared. Removed {} items.", before);
  }

  /**
   * Creates a full immutable snapshot of the current cache state.
   *
   * <p>This snapshot can be serialized and stored externally.</p>
   *
   * @return a complete {@link DatasetCacheSnapshot} of the current state
   */
  public DatasetCacheSnapshot<T, I> toSnapshot() {
    return new DatasetCacheSnapshot<>(
            new TreeMap<>(storage.getDatasetMap()),
            new TreeSet<>(storage.getEmptyIds()),
            progress.getMinId(),
            progress.getMaxId(),
            progress.getKnownCount(),
            progress.getExpectedTotal()
    );
  }
}
