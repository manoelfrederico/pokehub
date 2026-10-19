package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;

final class PokeApiUrls {

	private PokeApiUrls() {
	}

	static int extractId(String url) {
		if (url == null || url.isBlank()) {
			throw unavailable("Pokemon resource URL is blank", url);
		}
		String segment = lastSegment(stripQueryAndFragment(url.trim()));
		if (segment == null) {
			throw unavailable("Pokemon resource URL has no id", url);
		}
		return parseId(segment);
	}

	private static String stripQueryAndFragment(String url) {
		String withoutQuery = cutAt(url, '?');
		return cutAt(withoutQuery, '#');
	}

	private static String cutAt(String value, char marker) {
		int index = value.indexOf(marker);
		return index >= 0 ? value.substring(0, index) : value;
	}

	private static String lastSegment(String path) {
		String[] segments = path.split("/");
		for (int index = segments.length - 1; index >= 0; index--) {
			if (!segments[index].isEmpty()) {
				return segments[index];
			}
		}
		return null;
	}

	private static int parseId(String segment) {
		try {
			int id = Integer.parseInt(segment);
			if (id <= 0) {
				throw unavailable("Pokemon resource id must be positive but was " + id, segment);
			}
			return id;
		}
		catch (NumberFormatException exception) {
			throw new CatalogUnavailableException("Pokemon resource id is not numeric: " + segment, exception);
		}
	}

	private static CatalogUnavailableException unavailable(String message, String value) {
		return new CatalogUnavailableException(message, new IllegalArgumentException(String.valueOf(value)));
	}

}
