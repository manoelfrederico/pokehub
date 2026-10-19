package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.exception.PokemonNotFoundException;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonReferencePage;
import com.fred.pokehub.domain.model.PokemonSummary;
import com.fred.pokehub.domain.model.PokemonView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetPokemonDetailsServiceTest {

	@Test
	void shouldReturnAnUnsyncedView() {
		PokemonDetails details = details();
		FakeCatalog catalog = new FakeCatalog(details);
		GetPokemonDetailsService service = new GetPokemonDetailsService(catalog);

		PokemonView view = service.get(1);

		assertThat(view.details()).isEqualTo(details);
		assertThat(view.extras()).isNull();
		assertThat(view.synced()).isFalse();
		assertThat(catalog.requestedId).isEqualTo(1);
	}

	@Test
	void shouldRejectANonPositiveIdBeforeCallingTheCatalog() {
		FakeCatalog catalog = new FakeCatalog(details());
		GetPokemonDetailsService service = new GetPokemonDetailsService(catalog);

		assertThatThrownBy(() -> service.get(0))
				.isInstanceOf(InvalidPokemonIdException.class)
				.hasMessageContaining("0");
		assertThatThrownBy(() -> service.get(-1))
				.isInstanceOf(InvalidPokemonIdException.class)
				.hasMessageContaining("-1");
		assertThat(catalog.requestedId).isNull();
	}

	@Test
	void shouldPropagatePokemonNotFound() {
		PokemonNotFoundException failure = new PokemonNotFoundException(99);
		FakeCatalog catalog = new FakeCatalog(failure);
		GetPokemonDetailsService service = new GetPokemonDetailsService(catalog);

		assertThatThrownBy(() -> service.get(99)).isSameAs(failure);
	}

	@Test
	void shouldPropagateCatalogUnavailable() {
		CatalogUnavailableException failure = new CatalogUnavailableException("down", new IllegalStateException("down"));
		FakeCatalog catalog = new FakeCatalog(failure);
		GetPokemonDetailsService service = new GetPokemonDetailsService(catalog);

		assertThatThrownBy(() -> service.get(1)).isSameAs(failure);
	}

	@Test
	void shouldRejectANullCatalog() {
		assertThatThrownBy(() -> new GetPokemonDetailsService(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("catalog");
	}

	private static PokemonDetails details() {
		return new PokemonDetails(
				1,
				"bulbasaur",
				null,
				null,
				null,
				new Mass(69),
				List.of(),
				List.of(),
				null,
				new EvolutionNode(1, "bulbasaur", null, List.of()));
	}

	private static final class FakeCatalog implements PokemonCatalog {

		private final PokemonDetails details;

		private final RuntimeException failure;

		private Integer requestedId;

		private FakeCatalog(PokemonDetails details) {
			this.details = details;
			this.failure = null;
		}

		private FakeCatalog(RuntimeException failure) {
			this.details = null;
			this.failure = failure;
		}

		@Override
		public PokemonReferencePage listReferences(PageQuery query) {
			throw new UnsupportedOperationException("listReferences");
		}

		@Override
		public PokemonSummary findSummaryById(int id) {
			throw new UnsupportedOperationException("findSummaryById");
		}

		@Override
		public PokemonDetails findDetailsById(int id) {
			requestedId = id;
			if (failure != null) {
				throw failure;
			}
			return details;
		}

	}

}
