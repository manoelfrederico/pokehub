package com.fred.pokehub.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.LogicalType;

@Configuration
public class JsonMapperConfig {

	@Bean
	JsonMapperBuilderCustomizer strictRequestJson() {
		return JsonMapperConfig::apply;
	}

	/**
	 * Rejects unknown properties and numeric or boolean values where a string is required.
	 * Problem details render extra properties, including {@code errors}, at the top level.
	 */
	public static void apply(JsonMapper.Builder builder) {
		builder.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
				.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.withCoercionConfig(LogicalType.Textual, coercions -> coercions
						.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
						.setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
						.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
				.addMixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class);
	}

}
