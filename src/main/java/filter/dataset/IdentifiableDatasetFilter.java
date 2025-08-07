package filter.dataset;

import cache.core.Identifiable;
import filter.core.DatasetFilter;
import filter.core.FieldBinding;

import java.util.List;

public class IdentifiableDatasetFilter<T extends Identifiable<?>> implements DatasetFilter<T> {

  private final List<FieldBinding<T, ?>> bindings;

  public IdentifiableDatasetFilter(List<FieldBinding<T, ?>> bindings) {
    this.bindings = bindings;
  }

  @Override
  public boolean test(T value) {
    return bindings.stream().allMatch(a -> a.test(value));
  }

  @Override
  public String fingerprint() {
    return bindings.stream()
            .map(FieldBinding::fingerprint)
            .sorted()
            .reduce((a, b) -> a + "|" + b)
            .orElse("");
  }

  @Override
  public List<FieldBinding<T, ?>> getBindings() {
    return bindings;
  }
}
