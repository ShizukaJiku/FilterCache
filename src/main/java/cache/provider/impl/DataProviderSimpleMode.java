package cache.provider.impl;

import cache.core.Identifiable;
import cache.provider.DataProvider;
import cache.provider.DataProviderModeStrategy;
import cache.provider.DataRequest;
import cache.provider.DataResponse;

import java.util.function.Consumer;

public class DataProviderSimpleMode<T extends Identifiable<I>, I extends Comparable<I>>
        implements DataProviderModeStrategy<T, I> {

  @Override
  public DataResponse<T, I> getDataOfProvider(
          DataRequest<T, I> dataRequest, DataProvider<T, I> dataProvider) {
    return dataProvider.requestData(dataRequest, dataRequest.getPages().iterator().next());
  }

  @Override
  public void fetchData(
          DataRequest<T, I> dataRequest,
          DataProvider<T, I> dataProvider,
          Consumer<DataResponse<T, I>> responseConsumer) {
    for (var pageFetch : dataRequest.getPages()) {
      responseConsumer.accept(dataProvider.requestData(dataRequest, pageFetch));
    }
  }
}
