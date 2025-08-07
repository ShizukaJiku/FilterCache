package cache.provider.impl;

import cache.core.Identifiable;
import cache.provider.DataProvider;
import cache.provider.DataProviderModeStrategy;
import cache.provider.DataRequest;
import cache.provider.DataResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DataProviderParallelMode<T extends Identifiable<I>, I extends Comparable<I>> implements DataProviderModeStrategy<T, I> {

  private final ExecutorService executor = Executors.newFixedThreadPool(4);

  @Override
  public DataResponse<T, I> getDataOfProvider(
          DataRequest<T, I> dataRequest,
          DataProvider<T, I> dataProvider) {
    return dataProvider.requestData(dataRequest, dataRequest.getPages().iterator().next());
  }

  @Override
  public void fetchData(
          DataRequest<T, I> dataRequest,
          DataProvider<T, I> dataProvider,
          Consumer<DataResponse<T, I>> responseConsumer) {

    for (Integer page : dataRequest.getPages()) {
      CompletableFuture.runAsync(() -> {
        var response = dataProvider.requestData(dataRequest, page);
        responseConsumer.accept(response);
      }, executor);
    }
  }
}
