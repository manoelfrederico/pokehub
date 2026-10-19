package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.domain.model.PageQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pokemon")
public class PokemonController {

	private final ListPokemonUseCase listPokemon;

	public PokemonController(ListPokemonUseCase listPokemon) {
		this.listPokemon = listPokemon;
	}

	@GetMapping
	PokemonPageResponse list(@RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset) {
		return PokemonPageResponse.from(listPokemon.list(PageQuery.of(limit, offset)));
	}

}
