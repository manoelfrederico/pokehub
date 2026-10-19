package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.application.port.in.SyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UnsyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasCommand;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasUseCase;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.SyncResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pokemon")
public class PokemonController {

	private final ListPokemonUseCase listPokemon;

	private final GetPokemonDetailsUseCase details;

	private final SyncPokemonUseCase syncPokemon;

	private final UnsyncPokemonUseCase unsyncPokemon;

	private final UpdatePokemonExtrasUseCase updatePokemonExtras;

	public PokemonController(
			ListPokemonUseCase listPokemon,
			GetPokemonDetailsUseCase details,
			SyncPokemonUseCase syncPokemon,
			UnsyncPokemonUseCase unsyncPokemon,
			UpdatePokemonExtrasUseCase updatePokemonExtras
	) {
		this.listPokemon = listPokemon;
		this.details = details;
		this.syncPokemon = syncPokemon;
		this.unsyncPokemon = unsyncPokemon;
		this.updatePokemonExtras = updatePokemonExtras;
	}

	@GetMapping
	PokemonPageResponse list(@RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset) {
		return PokemonPageResponse.from(listPokemon.list(PageQuery.of(limit, offset)));
	}

	@GetMapping("/{id}")
	PokemonDetailsResponse details(@PathVariable int id) {
		return PokemonDetailsResponse.from(details.get(id));
	}

	@PostMapping("/{id}/sync")
	ResponseEntity<PokemonDetailsResponse> sync(@PathVariable int id) {
		SyncResult result = syncPokemon.sync(id);
		PokemonDetailsResponse body = PokemonDetailsResponse.from(result.view());
		if (result.created()) {
			return ResponseEntity.status(HttpStatus.CREATED).body(body);
		}
		return ResponseEntity.ok(body);
	}

	@DeleteMapping("/{id}/sync")
	ResponseEntity<Void> unsync(@PathVariable int id) {
		unsyncPokemon.unsync(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/extras")
	PokemonDetailsResponse updateExtras(@PathVariable int id, @Valid @RequestBody UpdateExtrasRequest request) {
		UpdatePokemonExtrasCommand command = new UpdatePokemonExtrasCommand(
				id,
				request.localizedName(),
				request.region(),
				request.tags());
		return PokemonDetailsResponse.from(updatePokemonExtras.update(command));
	}

}
