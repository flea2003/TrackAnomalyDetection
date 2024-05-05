package dev.system.backend.repositories;


import dev.system.backend.commons.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipRepository extends JpaRepository<Ship, Long> {
}
