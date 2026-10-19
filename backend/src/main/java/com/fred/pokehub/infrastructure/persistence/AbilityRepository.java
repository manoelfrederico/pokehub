package com.fred.pokehub.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AbilityRepository extends JpaRepository<AbilityEntity, Integer> {
}
