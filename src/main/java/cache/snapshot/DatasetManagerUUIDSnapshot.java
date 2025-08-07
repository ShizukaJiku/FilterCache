package cache.snapshot;

import cache.core.Identifiable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

@Getter
public class DatasetManagerUUIDSnapshot<T extends Identifiable<I>, I extends Comparable<I>> extends DatasetManagerSnapshot<T, I> implements Serializable {
  private final Map<String, I> uuidMap;

  public DatasetManagerUUIDSnapshot(DatasetCacheSnapshot<T, I> cacheSnapshot, Map<String, DatasetFilterManagerSnapshot<I>> filterSnapshots, Map<String, I> uuidMap) {
    super(cacheSnapshot, filterSnapshots);
    this.uuidMap = uuidMap;
  }
}
