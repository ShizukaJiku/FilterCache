package cache.snapshot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.NavigableSet;

/**
 * Immutable snapshot of a {@link cache.core.DatasetCacheManager} state,
 * designed for external persistence (e.g., Redis or database).
 *
 * <p>Includes:</p>
 * <ul>
 *   <li>All present dataset items, mapped by ID.</li>
 *   <li>IDs that are currently empty (known but without data).</li>
 *   <li>Progress metadata: known count, expected total, min and max IDs.</li>
 * </ul>
 *
 * <p>This class is serializable and can be used to fully reconstruct
 * the in-memory cache state.</p>
 *
 * @param <T> the entity type stored in the dataset
 * @param <I> the comparable identifier type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetCacheSnapshot<T, I extends Comparable<I>> implements Serializable {

  /**
   * Map of all present dataset items keyed by ID.
   */
  private NavigableMap<I, T> datasetMap;

  /**
   * Set of IDs that are known but currently empty.
   */
  private NavigableSet<I> emptyIds;

  /**
   * Smallest known ID in the dataset, or null if unknown.
   */
  private I minId;

  /**
   * Largest known ID in the dataset, or null if unknown.
   */
  private I maxId;

  /**
   * Number of dataset items currently known (present data).
   */
  private int knownCount;

  /**
   * Expected total number of dataset items, or -1 if unknown.
   */
  private int expectedTotal;
}
