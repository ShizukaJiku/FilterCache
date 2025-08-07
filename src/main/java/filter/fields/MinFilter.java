package filter.fields;

import filter.core.AbstractFieldFilter;

public class MinFilter<F extends Comparable<F>> extends AbstractFieldFilter<F> {

  public MinFilter(F filterValue) {
    super(filterValue, "min");
  }

  @Override
  public boolean test(F value) {
    return filterValue.compareTo(value) <= 0;
  }
}
