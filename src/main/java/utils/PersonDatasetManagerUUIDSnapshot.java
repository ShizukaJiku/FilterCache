package utils;

import cache.snapshot.DatasetCacheSnapshot;
import cache.snapshot.DatasetFilterManagerSnapshot;
import cache.snapshot.DatasetManagerUUIDSnapshot;

import java.util.Map;

public class PersonDatasetManagerUUIDSnapshot extends DatasetManagerUUIDSnapshot<Person, Integer> {
  public PersonDatasetManagerUUIDSnapshot(DatasetCacheSnapshot<Person, Integer> cacheSnapshot, Map<String, DatasetFilterManagerSnapshot<Integer>> filterSnapshots, Map<String, Integer> uuidMap) {
    super(cacheSnapshot, filterSnapshots, uuidMap);
  }
}
