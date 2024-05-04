package dev.system.backend.repositories;


import dev.system.backend.models.AIS;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipRepository extends JpaRepository<AIS, String> {
}
