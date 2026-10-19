package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.Stat;

public record StatResponse(String name, int baseValue) {

	public static StatResponse from(Stat stat) {
		return new StatResponse(stat.name(), stat.baseValue());
	}

}
