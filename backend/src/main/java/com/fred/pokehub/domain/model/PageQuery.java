package com.fred.pokehub.domain.model;

import com.fred.pokehub.domain.exception.InvalidPaginationException;

/**
 * A page window. Limit is between 1 and 50, offset is zero or greater.
 */
public record PageQuery(int limit, int offset) {

	public static final int DEFAULT_LIMIT = 20;

	public static final int MAX_LIMIT = 50;

	private static final int DEFAULT_OFFSET = 0;

	public PageQuery {
		if (limit < 1 || limit > MAX_LIMIT) {
			throw new InvalidPaginationException("limit must be between 1 and " + MAX_LIMIT + " but was " + limit);
		}
		if (offset < 0) {
			throw new InvalidPaginationException("offset must be >= 0 but was " + offset);
		}
	}

	public static PageQuery of(Integer limit, Integer offset) {
		int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
		int resolvedOffset = offset == null ? DEFAULT_OFFSET : offset;
		return new PageQuery(resolvedLimit, resolvedOffset);
	}

}
