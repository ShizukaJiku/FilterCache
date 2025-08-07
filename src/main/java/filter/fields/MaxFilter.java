package filter.fields;

import filter.core.AbstractFieldFilter;

public class MaxFilter<F extends Comparable<F>> extends AbstractFieldFilter<F> {

  public MaxFilter(F filterValue) {
    super(filterValue, "max");
  }

  @Override
  public boolean test(F value) {
    return filterValue.compareTo(value) >= 0;
  }
}
