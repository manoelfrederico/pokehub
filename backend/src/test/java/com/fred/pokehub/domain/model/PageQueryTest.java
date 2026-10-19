package com.fred.pokehub.domain.model;

import com.fred.pokehub.domain.exception.InvalidPaginationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageQueryTest {

	@Test
	void shouldApplyDefaultsWhenLimitAndOffsetAreMissing() {
		PageQuery query = PageQuery.of(null, null);

		assertThat(query.limit()).isEqualTo(20);
		assertThat(query.offset()).isZero();
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 50})
	void shouldAcceptLimitBoundaries(int limit) {
		assertThat(PageQuery.of(limit, 0).limit()).isEqualTo(limit);
	}

	@ParameterizedTest
	@ValueSource(ints = {0, 51, -1})
	void shouldRejectLimitOutsideRangeAndMentionTheValue(int limit) {
		assertThatThrownBy(() -> PageQuery.of(limit, 0))
				.isInstanceOf(InvalidPaginationException.class)
				.hasMessageContaining(Integer.toString(limit));
	}

	@Test
	void shouldRejectNegativeOffsetAndMentionTheValue() {
		assertThatThrownBy(() -> PageQuery.of(20, -1))
				.isInstanceOf(InvalidPaginationException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldAcceptZeroOffset() {
		assertThat(PageQuery.of(20, 0).offset()).isZero();
	}

	@Test
	void shouldAcceptMaxIntegerOffset() {
		assertThat(PageQuery.of(20, Integer.MAX_VALUE).offset()).isEqualTo(Integer.MAX_VALUE);
	}

}
