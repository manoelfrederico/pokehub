package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.application.port.in.SyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UnsyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasCommand;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasUseCase;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.SyncResult;
import com.fred.pokehub.web.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
@Tag(name = "pokemon")
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
	@Operation(summary = "List Pokémon")
	@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = OpenApiConfig.JSON, schema = @Schema(implementation = PokemonPageResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "502", description = "Catalog unavailable", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	PokemonPageResponse list(
			@Parameter(name = "limit", schema = @Schema(type = "integer", minimum = "1", maximum = "50", defaultValue = "20"))
			@RequestParam(name = "limit", required = false) Integer limit,
			@Parameter(name = "offset", schema = @Schema(type = "integer", minimum = "0", defaultValue = "0"))
			@RequestParam(name = "offset", required = false) Integer offset) {
		return PokemonPageResponse.from(listPokemon.list(PageQuery.of(limit, offset)));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get Pokémon details")
	@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = OpenApiConfig.JSON, schema = @Schema(implementation = PokemonDetailsResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "404", description = "Pokémon not found", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "502", description = "Catalog unavailable", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	PokemonDetailsResponse details(
			@Parameter(description = "Integer of at least 1. Alternative forms use ids of 10001 or greater.", schema = @Schema(type = "integer", minimum = "1"))
			@PathVariable int id) {
		return PokemonDetailsResponse.from(details.get(id));
	}

	@PostMapping("/{id}/sync")
	@SecurityRequirement(name = OpenApiConfig.BEARER)
	@Operation(summary = "Sync a Pokémon")
	@ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = OpenApiConfig.JSON, schema = @Schema(implementation = PokemonDetailsResponse.class)))
	@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = OpenApiConfig.JSON, schema = @Schema(implementation = PokemonDetailsResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "404", description = "Pokémon not found", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "502", description = "Catalog unavailable", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	ResponseEntity<PokemonDetailsResponse> sync(
			@Parameter(description = "Integer of at least 1. Alternative forms use ids of 10001 or greater.", schema = @Schema(type = "integer", minimum = "1"))
			@PathVariable int id) {
		SyncResult result = syncPokemon.sync(id);
		PokemonDetailsResponse body = PokemonDetailsResponse.from(result.view());
		if (result.created()) {
			return ResponseEntity.status(HttpStatus.CREATED).body(body);
		}
		return ResponseEntity.ok(body);
	}

	@DeleteMapping("/{id}/sync")
	@SecurityRequirement(name = OpenApiConfig.BEARER)
	@Operation(summary = "Remove a synced Pokémon")
	@ApiResponse(responseCode = "204", description = "No content")
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "404", description = "Pokémon not stored", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	ResponseEntity<Void> unsync(
			@Parameter(description = "Integer of at least 1. Alternative forms use ids of 10001 or greater.", schema = @Schema(type = "integer", minimum = "1"))
			@PathVariable int id) {
		unsyncPokemon.unsync(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/extras")
	@SecurityRequirement(name = OpenApiConfig.BEARER)
	@Operation(summary = "Replace Pokémon extras")
	@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = OpenApiConfig.JSON, schema = @Schema(implementation = PokemonDetailsResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad request. Validation failures add errors.", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "404", description = "Pokémon not stored", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "415", description = "Unsupported media type", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	PokemonDetailsResponse updateExtras(
			@Parameter(description = "Integer of at least 1. Alternative forms use ids of 10001 or greater.", schema = @Schema(type = "integer", minimum = "1"))
			@PathVariable int id,
			@Valid @RequestBody UpdateExtrasRequest request) {
		UpdatePokemonExtrasCommand command = new UpdatePokemonExtrasCommand(
				id,
				request.localizedName(),
				request.region(),
				request.tags());
		return PokemonDetailsResponse.from(updatePokemonExtras.update(command));
	}

}
