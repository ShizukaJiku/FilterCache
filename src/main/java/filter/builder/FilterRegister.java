package filter.builder;

import filter.core.FieldResolver;

import java.util.HashMap;
import java.util.Map;

public class FilterRegister {
  private static final Map<FieldMap, FieldResolver<?, ?>> FIELD_RESOLVER_MAP = new HashMap<>();

  public static <T, F> void registerFieldResolver(
          Class<T> tClass, String name, FieldResolver<T, F> resolver) {

    var key = new FieldMap(tClass, name);

    if (FIELD_RESOLVER_MAP.containsKey(key) && resolver != FIELD_RESOLVER_MAP.get(key)) {
      System.out.println("Duplicate field resolver found for " + key + ". Resolver be replace");
    }

    FIELD_RESOLVER_MAP.put(key, resolver);
  }

  @SuppressWarnings("unchecked")
  static <T> FieldResolver<T, ?> getFieldResolver(Class<T> tClass, String name) {
    return (FieldResolver<T, ?>) FIELD_RESOLVER_MAP.get(new FieldMap(tClass, name));
  }

  private record FieldMap(Class<?> tClass, String name) {
  }
}
