package filter.core;

public final class FieldBinding<T, F> implements Criterion<T> {
  private final String fieldName;
  private final FieldFilter<F> filter;
  private final FieldResolver<T, F> resolver;

  public FieldBinding(String fieldName, FieldFilter<F> filter, FieldResolver<T, F> resolver) {
    this.fieldName = fieldName;
    this.filter = filter;
    this.resolver = resolver;
  }

  @Override
  public boolean test(T entity) {
    return filter.test(resolver.resolve(entity));
  }

  @Override
  public String fingerprint() {
    return fieldName + ":" + filter.fingerprint();
  }

  public String getFieldName() {
    return fieldName;
  }

  public FieldFilter<F> getFilter() {
    return filter;
  }

  public FieldResolver<T, F> getResolver() {
    return resolver;
  }
}
