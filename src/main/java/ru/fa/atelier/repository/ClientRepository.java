package ru.fa.atelier.repository;

import ru.fa.atelier.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUser_Email(String email);
}
