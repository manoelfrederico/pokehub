package com.fred.pokehub.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeciesRepository extends JpaRepository<SpeciesEntity, Integer> {
}
