package com.fred.pokehub.application.port.in;

import java.util.List;

/**
 * Input for replacing the proprietary fields of one stored Pokémon.
 *
 * @param tags raw tags; {@code null} means none
 */
public record UpdatePokemonExtrasCommand(int id, String localizedName, String region, List<String> tags) {
}
