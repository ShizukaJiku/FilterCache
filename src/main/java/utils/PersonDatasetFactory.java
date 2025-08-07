package utils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PersonDatasetFactory {

  private static final String[] FIRST_NAMES = {
          "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank",
          "Grace", "Hugo", "Ivy", "John", "Karen", "Leo",
          "Mia", "Nick", "Olivia", "Paul", "Quinn", "Rita",
          "Steve", "Trudy", "Uma", "Victor", "Wendy", "Xavier",
          "Yara", "Zack"
  };

  /**
   * Crea un dataset de tamaño indicado con valores pseudoaleatorios.
   *
   * @param size cantidad de personas
   * @return lista de personas generadas
   */
  public static List<Person> createSampleDataset(int size) {
    return createSampleDataset(size, System.currentTimeMillis(), 1970, 2005);
  }

  /**
   * Crea un dataset pseudoaleatorio reproducible
   *
   * @param size    número de registros
   * @param seed    semilla de aleatoriedad para reproducibilidad
   * @param minYear año mínimo de nacimiento
   * @param maxYear año máximo de nacimiento
   * @return lista de personas
   */
  public static List<Person> createSampleDataset(int size, long seed, int minYear, int maxYear) {
    Set<Integer> peopleID = new HashSet<>(size);
    Set<Person> people = new HashSet<>(size);

    Random random = new Random(seed);

    while (people.size() < size) {
      // Nombre aleatorio
      String name = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];

      // Fecha de nacimiento aleatoria
      int year = minYear + random.nextInt(maxYear - minYear + 1);
      int month = 1 + random.nextInt(12);
      int day = 1 + random.nextInt(28); // Evita problemas con febrero
      LocalDate birthDate = LocalDate.of(year, month, day);

      // Salario con distribución 1000-10000
      double salary = Math.round((1000 + random.nextDouble() * 9000) * 100.0) / 100.0;

      // Tipo de persona aleatorio
      var type = random.nextBoolean() ? PersonType.NATURAL : PersonType.LEGAL;
      var status = random.nextBoolean() ? "VALID" : "INVALID";

      var id = random.nextInt(size * 10);

      if (!peopleID.contains(id)) {
        peopleID.add(id);
        people.add(new Person(id, name, salary, birthDate, type));
      }
    }

    return people.stream().sorted(Comparator.comparingInt(Person::getId))
            .collect(Collectors.toList());
  }
}
