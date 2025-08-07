package cache.snapshot;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.BitSet;
import java.util.List;

@Getter
@AllArgsConstructor
public class DatasetFilterManagerSnapshot<I extends Comparable<I>> implements Serializable {
  private final int totalElements;
  private final int knownCount;
  private final List<I> idStorage;
  private final BitSet populatedPositions;
  private final BitSet populatedPages;
}