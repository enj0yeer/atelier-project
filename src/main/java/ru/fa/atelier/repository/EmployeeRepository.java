package ru.fa.atelier.repository;

import ru.fa.atelier.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAllByActiveTrueOrderByFullNameAsc();
    Optional<Employee> findByUser_Email(String email);
}
