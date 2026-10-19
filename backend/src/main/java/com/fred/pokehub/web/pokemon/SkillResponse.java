package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.Ability;

public record SkillResponse(String name, boolean hidden) {

	public static SkillResponse from(Ability ability) {
		return new SkillResponse(ability.name(), ability.hidden());
	}

}
