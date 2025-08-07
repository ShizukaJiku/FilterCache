package cache.provider;

import cache.core.Identifiable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single or multi-page data response from a {@link DataProvider}.
 *
 * <p>Includes:
 * <ul>
 *   <li>The page data (entities)</li>
 *   <li>Page and page size information</li>
 *   <li>Total filtered count for the current filter</li>
 *   <li>Global dataset metadata (min/max ID and total size)</li>
 * </ul>
 *
 * <p>This object is immutable and safe to cache.
 *
 * @param <T>  the dataset entity type
 * @param <ID> the type of the entity identifier
 */
@Getter
@RequiredArgsConstructor
public class DataResponse<T extends Identifiable<ID>, ID extends Comparable<ID>> {

  /**
   * The page data (never null, may be empty).
   */
  private final List<T> data;
  /**
   * The 1-based page index of this data.
   */
  private final int page;
  /**
   * The page size used for the request.
   */
  private final int pageSize;
  /**
   * Number of entities matching the applied filter.
   */
  private final int totalFiltered;
  /**
   * Total number of entities in the full dataset.
   */
  private final int totalDataset;

  /**
   * Cached immutable list of IDs (lazily initialized).
   */
  private transient List<ID> cachedIds;

  /**
   * Extracts the list of IDs for the entities in this page.
   *
   * @return immutable list of entity IDs
   */
  public List<ID> getDataID() {
    if (cachedIds == null) {
      List<ID> ids = new ArrayList<>(data.size());
      for (T entity : data) {
        ids.add(entity.getId());
      }
      cachedIds = Collections.unmodifiableList(ids);
    }
    return cachedIds;
  }
}
