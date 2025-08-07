package filter.core;

import java.util.List;

public interface DatasetFilter<T> extends Criterion<T> {
  List<FieldBinding<T, ?>> getBindings();
}
