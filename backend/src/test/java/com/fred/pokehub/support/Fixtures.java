package com.fred.pokehub.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class Fixtures {

	private Fixtures() {
	}

	public static String read(String classpathResource) {
		InputStream stream = Fixtures.class.getClassLoader().getResourceAsStream(classpathResource);
		if (stream == null) {
			throw new IllegalArgumentException("Classpath resource not found: " + classpathResource);
		}
		try (stream) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Failed to read classpath resource: " + classpathResource, exception);
		}
	}

}
