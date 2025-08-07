package filter.fields;

import filter.core.AbstractFieldFilter;

import java.util.Objects;

public class EqualsFilter<F> extends AbstractFieldFilter<F> {

  public EqualsFilter(F expected) {
    super(expected, "equals");
  }

  @Override
  public boolean test(F value) {
    return Objects.equals(filterValue, value);
  }
}
