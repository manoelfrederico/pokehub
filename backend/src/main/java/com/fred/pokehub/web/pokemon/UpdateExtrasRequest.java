package com.fred.pokehub.web.pokemon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Full replacement of proprietary fields. Absent tags mean none.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record UpdateExtrasRequest(
		String localizedName,
		String region,
		List<@NotNull(message = "must not be null") @NotBlank(message = "must not be blank") String> tags
) {
}
