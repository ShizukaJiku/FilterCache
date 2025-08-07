package filter.core;

public interface FieldFilter<F> extends Criterion<F> {
  String getFilterKey();

  F getFilterValue();
}
