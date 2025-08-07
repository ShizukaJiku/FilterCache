package filter.builder;

import cache.core.Identifiable;
import filter.core.FieldBinding;
import filter.core.FieldFilter;
import filter.core.FieldResolver;
import filter.dataset.IdentifiableDatasetFilter;

import java.util.ArrayList;
import java.util.List;

public class IdentifiableDatasetFilterBuilder<T extends Identifiable<?>> {

  private final List<FieldBinding<T, ?>> bindings = new ArrayList<>();
  private final Class<T> clazz;

  private IdentifiableDatasetFilterBuilder(Class<T> clazz) {
    this.clazz = clazz;
  }

  public static <T extends Identifiable<?>> IdentifiableDatasetFilterBuilder<T> builder(Class<T> entityClass) {
    return new IdentifiableDatasetFilterBuilder<>(entityClass);
  }

  @SuppressWarnings("unchecked")
  public <F> IdentifiableDatasetFilterBuilder<T> with(String fieldName, FieldFilter<F> filter) {
    bindings.add(
            new FieldBinding<>(
                    fieldName,
                    filter,
                    (FieldResolver<T, F>) FilterRegister.getFieldResolver(clazz, fieldName)));
    return this;
  }

  public IdentifiableDatasetFilter<T> build() {
    return new IdentifiableDatasetFilter<>(bindings);
  }
}
