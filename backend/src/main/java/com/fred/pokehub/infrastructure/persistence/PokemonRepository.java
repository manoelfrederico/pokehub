package com.fred.pokehub.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface PokemonRepository extends JpaRepository<PokemonEntity, Integer> {

	@Override
	@EntityGraph(attributePaths = {"species", "abilities", "abilities.ability", "stats", "tags"})
	Optional<PokemonEntity> findById(Integer id);

	@Query("select p.id from PokemonEntity p where p.id in :ids")
	Set<Integer> findIdsByIdIn(@Param("ids") Collection<Integer> ids);

}
