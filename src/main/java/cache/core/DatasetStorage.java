package cache.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Handles storage of present dataset items and their "empty" (known but missing) IDs.
 *
 * <p>This class is a low-level storage component for {@link DatasetCacheManager}:</p>
 * <ul>
 *   <li>Stores present items in a {@link NavigableMap} for efficient ordered access.</li>
 *   <li>Tracks known IDs without data in a {@link NavigableSet} (gaps).</li>
 *   <li>Provides immutable views for safe external access (snapshots, caching).</li>
 * </ul>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * DatasetStorage<User, Long> storage = new DatasetStorage<>();
 * storage.put(new User(1L, "Alice"));
 *
 * if (storage.contains(1L)) {
 *     log.info("User 1 is in cache!");
 * }
 *
 * storage.remove(1L);
 * log.info("Cache size: {}", storage.size());
 * }</pre>
 *
 * @param <T> dataset entity type, must implement {@link Identifiable}
 * @param <I> ID type, must implement {@link Comparable} for ordered storage
 */
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.MODULE)
@AllArgsConstructor(access = AccessLevel.MODULE)
public class DatasetStorage<T extends Identifiable<I>, I extends Comparable<I>> {

  /**
   * Map of present items (ordered by ID).
   * <p>Mutable internally; external code should use {@link #asMap()} or snapshot methods for safety.</p>
   */
  private NavigableMap<I, T> datasetMap = new TreeMap<>();

  /**
   * IDs that are known but currently do not have an associated element (gaps).
   * <p>Used for predictive caching and determining missing elements.</p>
   */
  private NavigableSet<I> emptyIds = new TreeSet<>();

  /**
   * Adds or replaces an item in storage.
   * <ul>
   *   <li>Removes the ID from {@link #emptyIds} if present.</li>
   *   <li>Overwrites existing entries with the same ID.</li>
   * </ul>
   *
   * @param item item to store (must have non-null ID)
   */
  public void put(T item) {
    I id = item.getId();
    datasetMap.put(id, item);
    emptyIds.remove(id);

    log.trace("Stored item with ID {}", id);
  }

  /**
   * Removes an item from storage and marks its ID as empty.
   *
   * @param id ID of the item to remove
   */
  public void remove(I id) {
    datasetMap.remove(id);
    emptyIds.add(id);
    log.trace("Removed item with ID {}, marked as empty", id);
  }

  /**
   * Checks if a given ID has an associated item in the dataset map.
   *
   * @param id ID to check
   * @return true if the ID has an associated item
   */
  public boolean contains(I id) {
    return datasetMap.containsKey(id);
  }

  /**
   * Returns a list of known items for the given IDs.
   * <p>Null IDs are ignored, and missing IDs are skipped.</p>
   *
   * @param ids collection of IDs to look up
   * @return list of present items (may be empty, never null)
   */
  public List<T> getValues(Collection<I> ids) {
    List<T> result = new ArrayList<>(ids.size());
    for (I id : ids) {
      if (id == null) continue;
      T val = datasetMap.get(id);
      if (val != null) result.add(val);
    }
    return result;
  }

  /**
   * Returns an immutable list of all known values.
   */
  public List<T> getAllValues() {
    return List.copyOf(datasetMap.values());
  }

  /**
   * Returns an immutable set of all known IDs (only those with items).
   */
  public Set<I> getAllIds() {
    return Set.copyOf(datasetMap.keySet());
  }

  /**
   * Returns an unmodifiable map view of the current dataset.
   * <p>Useful for creating snapshots without allowing external modification.</p>
   */
  public NavigableMap<I, T> asMap() {
    return Collections.unmodifiableNavigableMap(datasetMap);
  }

  /**
   * @return number of stored elements
   */
  public int size() {
    return datasetMap.size();
  }

  /**
   * @return true if there are no stored elements
   */
  public boolean isEmpty() {
    return datasetMap.isEmpty();
  }

  /**
   * Clears all storage, removing both present items and empty IDs.
   */
  public void clear() {
    int removed = datasetMap.size();
    datasetMap.clear();
    emptyIds.clear();
    log.debug("Cleared DatasetStorage, removed {} items", removed);
  }
}
