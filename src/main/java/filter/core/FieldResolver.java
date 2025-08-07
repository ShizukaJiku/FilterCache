package filter.core;

@FunctionalInterface
public interface FieldResolver<T, F> {
  F resolve(T entity);
}
