package main;

import cache.core.DatasetManager;
import cache.manager.DatasetManagerUUID;
import cache.provider.DataRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import filter.builder.FilterRegister;
import filter.builder.IdentifiableDatasetFilterBuilder;
import filter.core.DatasetFilter;
import filter.fields.ContainsFilter;
import filter.fields.EqualsFilter;
import filter.fields.MaxFilter;
import filter.fields.MinFilter;
import utils.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Main {

  public static void main(String[] args) {

    DatasetManagerUUID<Person, Integer> datasetManager = new DatasetManagerUUID<>();

    var uuidMap = datasetManager.getUuidMap();

    // Registrar resolvers
    FilterRegister.registerFieldResolver(Person.class, "name", Person::getName);
    FilterRegister.registerFieldResolver(Person.class, "salary", Person::getSalary);
    FilterRegister.registerFieldResolver(Person.class, "birthDate", Person::getBirthDate);
    FilterRegister.registerFieldResolver(Person.class, "type", Person::getType);

    // ====== 1️⃣ Filtro 1: nombre contiene "a" y salario 2000-5000 ======
    var filter1 = IdentifiableDatasetFilterBuilder.builder(Person.class)
            .with("name", new ContainsFilter("a"))
            .with("salary", new MinFilter<>(2000.0))
            .with("salary", new MaxFilter<>(5000.0))
            .build();

    // ====== 2️⃣ Filtro 2: nombre con regex .*b.* y salario 2000-5000 ======
    var filter2 = IdentifiableDatasetFilterBuilder.builder(Person.class)
            .with("salary", new MinFilter<>(2000.0))
            .with("salary", new MaxFilter<>(5000.0))
            .build();

    // ====== 3️⃣ Filtro 3: Solo tipo LEGAL ======
    var filter3 = IdentifiableDatasetFilterBuilder.builder(Person.class)
            .with("type", new EqualsFilter<>(PersonType.LEGAL))
            .build();

    // ====== 4️⃣ Filtro 4: salario menor que 2000 ======
    var filter4 = IdentifiableDatasetFilterBuilder.builder(Person.class)
            .with("salary", new MinFilter<>(0.0))
            .with("salary", new MaxFilter<>(2000.0))
            .build();

    // ====== 6️⃣ Filtro 5 fecha de nacimiento en un rango específico ======
    var filter5 = IdentifiableDatasetFilterBuilder.builder(Person.class)
            .with("birthDate", new MinFilter<>(LocalDate.of(1998, 1, 1)))
            .with("birthDate", new MaxFilter<>(LocalDate.of(2000, 1, 1)))
            .with("type", new EqualsFilter<>(PersonType.NATURAL))
            .build();

    // Crear provider
    var dataProvider = new PersonDataProviderImpl();

    // Mostrar DATASET completo
    var dataset = dataProvider.getDataset();
    System.out.println("=== Dataset completo ===");
    dataset.forEach(System.out::println);
    System.out.println("Dataset size -> " + dataset.size());

    // Aplicar filtros y paginación
    testFilterWithPagination(datasetManager, filter1, dataProvider);
    testFilterWithPagination(datasetManager, filter2, dataProvider);
    testFilterWithPagination(datasetManager, filter3, dataProvider);
    testFilterWithPagination(datasetManager, filter4, dataProvider);
    testFilterWithPagination(datasetManager, filter5, dataProvider);

    System.out.println("Data no cacheada -> ");


    var cachedData = datasetManager.getCachedData();
    var idListCachedData = cachedData.stream().map(Person::getId).toList();

    dataProvider.getDataset().stream().filter(person -> !idListCachedData.contains(person.getId())).forEach(System.out::println);

    System.out.println("UUID map -> ");
    System.out.println("Size -> " + uuidMap.size());
    uuidMap.forEach((var key, var value) -> System.out.println("UUID -> " + key + "\t Value -> " + value));

    System.out.println("Find Data");
    System.out.println("Random UUID -> " + datasetManager.findByUUID(List.of(UUID.randomUUID().toString())));
    System.out.println("Present UUID -> " + datasetManager.findByUUID(List.of(uuidMap.keySet().iterator().next())));

    var datasetManagerSnapshot = datasetManager.toSnapshot();
    System.out.println("Dataset snapshot -> " + datasetManagerSnapshot);

    // SERIALIZACIÓN
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .create();
    String json = gson.toJson(datasetManagerSnapshot);
    System.out.println("Serialized snapshot (Redis-like):\n" + json);

    // SIMULAR que este json fue guardado en Redis y recuperado luego
    String restoredJson = json;

    // DESERIALIZACIÓN
    var restoredSnapshot = gson.fromJson(restoredJson, PersonDatasetManagerUUIDSnapshot.class);

    var newDatasetManager = DatasetManagerUUID.fromSnapshot(restoredSnapshot);
    System.out.println("New DatasetManager -> " + newDatasetManager);

    var datasetEquals = newDatasetManager.equals(datasetManager);
    System.out.println("Dataset equals -> " + datasetEquals);
  }

  private static void testFilterWithPagination(DatasetManager<Person, Integer> datasetManager,
                                               DatasetFilter<Person> filter,
                                               PersonDataProviderImpl dataProvider) {
    int pageSize = 25;
    int totalData = dataProvider.requestData(new DataRequest<>(filter, 1, pageSize), 1).getTotalFiltered();
    int totalPages = (int) Math.ceil((double) totalData / pageSize);

    for (int page = 1; page <= totalPages; page++) {
      System.out.printf("\n=== Resultados para %s - Página %d ===\n", filter.fingerprint(), page);
      List<Person> dataFiltered = datasetManager.getData(filter, page, pageSize, dataProvider);
      dataFiltered.forEach(System.out::println);
      System.out.printf("Total en página %d: %d\n", page, dataFiltered.size());

      // Mostrar estado de la caché después de cada solicitud
      showCacheState(datasetManager);
    }
  }

  private static void showCacheState(DatasetManager<Person, Integer> datasetManager) {
    System.out.println("\n=== Estado de la caché ===");
    //datasetManager.getCacheState();
  }
}
