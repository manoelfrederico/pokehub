package com.fred.pokehub.web.pokemon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Full replacement of proprietary fields. Absent tags mean none.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
@Schema(description = "Full replacement. Unknown properties are rejected.")
public record UpdateExtrasRequest(
		@Schema(description = "Optional, trimmed. Blank becomes null.", maxLength = 100, nullable = true) String localizedName,
		@Schema(description = "Optional, trimmed. Blank becomes null.", maxLength = 100, nullable = true) String region,
		@ArraySchema(maxItems = 10, schema = @Schema(minLength = 1, maxLength = 30), arraySchema = @Schema(description = "Optional. Absent means none. Trimmed, lower-cased, no duplicates."))
		List<@NotNull(message = "must not be null") @NotBlank(message = "must not be blank") String> tags
) {
}
