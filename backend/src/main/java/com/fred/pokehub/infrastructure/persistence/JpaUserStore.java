package com.fred.pokehub.infrastructure.persistence;

import com.fred.pokehub.application.exception.UsernameAlreadyTakenException;
import com.fred.pokehub.application.port.out.UserStore;
import com.fred.pokehub.domain.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaUserStore implements UserStore {

	private static final String USERNAME_UNIQUE_CONSTRAINT = "uq_users_username";

	private final UserRepository users;

	public JpaUserStore(UserRepository users) {
		this.users = Objects.requireNonNull(users, "users");
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findByUsername(String username) {
		return users.findByUsername(normalized(username)).map(UserMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		return users.existsByUsername(normalized(username));
	}

	@Override
	@Transactional
	public User save(User user) {
		Objects.requireNonNull(user, "user");
		try {
			return UserMapper.toDomain(users.save(UserMapper.toEntity(user)));
		}
		catch (DataIntegrityViolationException exception) {
			if (isUsernameTaken(exception)) {
				throw new UsernameAlreadyTakenException(user.username());
			}
			throw exception;
		}
	}

	private static String normalized(String username) {
		Objects.requireNonNull(username, "username");
		return username.strip().toLowerCase(Locale.ROOT);
	}

	private static boolean isUsernameTaken(DataIntegrityViolationException exception) {
		Throwable cause = exception.getMostSpecificCause();
		String message = cause.getMessage();
		return message != null && message.contains(USERNAME_UNIQUE_CONSTRAINT);
	}

}
