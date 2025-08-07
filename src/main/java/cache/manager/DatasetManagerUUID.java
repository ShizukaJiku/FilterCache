package cache.manager;

import cache.core.DatasetCacheManager;
import cache.core.DatasetFilterManager;
import cache.core.DatasetManager;
import cache.core.Identifiable;
import cache.snapshot.DatasetManagerUUIDSnapshot;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class DatasetManagerUUID<T extends Identifiable<I>, I extends Comparable<I>> extends DatasetManager<T, I> {
  private final Map<String, I> uuidMap;

  public DatasetManagerUUID() {
    this(new DatasetCacheManager<>(), new ConcurrentHashMap<>(), new HashMap<>());
  }

  private DatasetManagerUUID(DatasetCacheManager<T, I> datasetRaw, Map<String, DatasetFilterManager<I>> mapDatasetFilterManager, Map<String, I> uuidMap) {
    super(datasetRaw, mapDatasetFilterManager, (var entity) -> uuidMap.put(UUID.randomUUID().toString(), entity.getId()));
    this.uuidMap = uuidMap;
  }

  public static <T extends Identifiable<I>, I extends Comparable<I>> DatasetManagerUUID<T, I> fromSnapshot(DatasetManagerUUIDSnapshot<T, I> snapshot) {
    return new DatasetManagerUUID<>(DatasetCacheManager.fromSnapshot(snapshot.getCacheSnapshot()),
            snapshot.getFilterSnapshots().entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    v -> DatasetFilterManager.fromSnapshot(v.getValue()))), snapshot.getUuidMap());
  }

  public List<T> findByUUID(List<String> uuidList) {
    return this.findByID(uuidList.stream().map(uuidMap::get).toList());
  }

  @Override
  public DatasetManagerUUIDSnapshot<T, I> toSnapshot() {
    return new DatasetManagerUUIDSnapshot<>(
            datasetRaw.toSnapshot(),
            mapDatasetFilterManager.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().toSnapshot())), uuidMap);
  }
}
