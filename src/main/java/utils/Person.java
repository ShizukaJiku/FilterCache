package utils;

import cache.core.Identifiable;

import java.time.LocalDate;

public class Person implements Identifiable<Integer> {
  private Integer id;
  private String name;
  private Double salary;
  private LocalDate birthDate;
  private PersonType type;

  public Person(Integer id, String name, Double salary, LocalDate birthDate, PersonType type) {
    this.id = id;
    this.name = name;
    this.salary = salary;
    this.birthDate = birthDate;
    this.type = type;
  }

  @Override
  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Double getSalary() {
    return salary;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public PersonType getType() {
    return type;
  }

  @Override
  public String toString() {
    return "Person{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", salary=" + salary +
            ", birthDate=" + birthDate +
            ", type=" + type +
            '}';
  }
}
