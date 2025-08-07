package filter.fields;

import filter.core.AbstractFieldFilter;

public class ContainsFilter extends AbstractFieldFilter<String> {

  public ContainsFilter(String filterValue) {
    super(filterValue, "contains");
  }

  @Override
  public boolean test(String value) {
    return value != null && value.contains(filterValue);
  }
}
