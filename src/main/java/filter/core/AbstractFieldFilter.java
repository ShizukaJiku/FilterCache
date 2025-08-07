package filter.core;

public abstract class AbstractFieldFilter<F> implements FieldFilter<F> {
  protected final F filterValue;
  private final String filterKey;

  protected AbstractFieldFilter(F filterValue, String filterKey) {
    this.filterValue = filterValue;
    this.filterKey = filterKey;
  }


  @Override
  public String getFilterKey() {
    return filterKey;
  }

  @Override
  public F getFilterValue() {
    return filterValue;
  }

  @Override
  public String fingerprint() {
    return filterKey + ":" + filterValue;
  }
}
