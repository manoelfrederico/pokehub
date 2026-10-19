package com.fred.pokehub.infrastructure.persistence;

import com.fred.pokehub.application.exception.UsernameAlreadyTakenException;
import com.fred.pokehub.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaUserStoreTest {

	private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private static final User ASH = new User(ID, "ash", "hashed");

	@Mock
	private UserRepository users;

	private JpaUserStore store;

	@BeforeEach
	void setUp() {
		store = new JpaUserStore(users);
	}

	@Test
	void shouldSaveAUser() {
		when(users.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User saved = store.save(ASH);

		assertThat(saved).isEqualTo(ASH);
		UserEntity entity = savedEntity();
		assertThat(entity.id()).isEqualTo(ID);
		assertThat(entity.username()).isEqualTo("ash");
		assertThat(entity.passwordHash()).isEqualTo("hashed");
	}

	@Test
	void shouldMapADuplicateKeyToUsernameAlreadyTaken() {
		when(users.save(any(UserEntity.class))).thenThrow(new DataIntegrityViolationException(
				"duplicate",
				new RuntimeException("ERROR: duplicate key value violates unique constraint \"uq_users_username\"")));

		assertThatThrownBy(() -> store.save(ASH))
				.isInstanceOf(UsernameAlreadyTakenException.class)
				.hasMessage("Username already taken");
	}

	@Test
	void shouldPropagateOtherStoreFailures() {
		DataIntegrityViolationException failure = new DataIntegrityViolationException(
				"other",
				new RuntimeException("ERROR: null value in column \"id\""));
		when(users.save(any(UserEntity.class))).thenThrow(failure);

		assertThatThrownBy(() -> store.save(ASH)).isSameAs(failure);
	}

	@Test
	void shouldFindByUsernameInLowerCase() {
		UserEntity entity = UserMapper.toEntity(ASH);
		when(users.findByUsername("ash")).thenReturn(Optional.of(entity));

		assertThat(store.findByUsername("ASH")).contains(ASH);
		verify(users).findByUsername("ash");
	}

	@Test
	void shouldCheckExistenceByUsernameInLowerCase() {
		when(users.existsByUsername("ash")).thenReturn(true);

		assertThat(store.existsByUsername("ASH")).isTrue();
		verify(users).existsByUsername("ash");
	}

	@Test
	void shouldReturnEmptyWhenTheUsernameIsUnknown() {
		when(users.findByUsername("misty")).thenReturn(Optional.empty());

		assertThat(store.findByUsername("misty")).isEmpty();
	}

	private UserEntity savedEntity() {
		ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
		verify(users).save(captor.capture());
		return captor.getValue();
	}

}
