package utils;

import cache.provider.DataProvider;
import cache.provider.DataRequest;
import cache.provider.DataResponse;
import filter.core.DatasetFilter;

import java.util.List;

public class PersonDataProviderImpl implements DataProvider<Person, Integer> {
  private static final List<Person> DATASET = PersonDatasetFactory.createSampleDataset(100);
  private static final Integer DATASET_SIZE = DATASET.size();

  @Override
  public DataResponse<Person, Integer> requestData(DataRequest<Person, Integer> dataRequest, Integer page) {
    if (dataRequest == null || dataRequest.getDatasetFilter() == null) {
      throw new IllegalArgumentException("DataRequest y su filtro no pueden ser nulos.");
    }

    if (page <= 0) {
      throw new IllegalArgumentException("El número de página debe ser mayor que 0.");
    }

    var totalDataFiltered = getFilteredDataset(dataRequest.getDatasetFilter());
    var totalFiltered = totalDataFiltered.size();

    // Verificar si hay datos filtrados
    if (totalFiltered == 0) {
      return new DataResponse<>(List.of(), page, dataRequest.getPageSize(), totalFiltered, DATASET_SIZE);
    }

    var fromIndex = (page - 1) * dataRequest.getPageSize();
    var toIndex = Math.min(page * dataRequest.getPageSize(), totalFiltered);

    // Verificar límites de índices
    if (fromIndex >= totalFiltered) {
      return new DataResponse<>(List.of(), page, dataRequest.getPageSize(), totalFiltered, DATASET_SIZE);
    }

    var dataFiltered = totalDataFiltered.subList(fromIndex, toIndex);

    return new DataResponse<>(dataFiltered, page, dataRequest.getPageSize(), totalFiltered, DATASET_SIZE);
  }


  public List<Person> getDataset() {
    return DATASET;
  }

  private List<Person> getFilteredDataset(DatasetFilter<Person> datasetFilter) {
    return getDataset().stream().filter(datasetFilter::test).toList();
  }
}
