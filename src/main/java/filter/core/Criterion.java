package filter.core;

public interface Criterion<T> {

  boolean test(T value);

  String fingerprint();
}
