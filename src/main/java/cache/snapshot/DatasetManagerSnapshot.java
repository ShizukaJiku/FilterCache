package cache.snapshot;

import cache.core.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class DatasetManagerSnapshot<T extends Identifiable<I>, I extends Comparable<I>> implements Serializable {
  /**
   * Global cache snapshot
   */
  private DatasetCacheSnapshot<T, I> cacheSnapshot;

  /**
   * Per-filter cache state
   */
  private Map<String, DatasetFilterManagerSnapshot<I>> filterSnapshots;
}
