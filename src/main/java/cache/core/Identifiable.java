package cache.core;

public interface Identifiable<I extends Comparable<I>> {
  I getId();
}
