package com.fred.pokehub.infrastructure.persistence;

import com.fred.pokehub.domain.model.User;

final class UserMapper {

	private UserMapper() {
	}

	static UserEntity toEntity(User user) {
		UserEntity entity = new UserEntity();
		entity.setId(user.id());
		entity.setUsername(user.username());
		entity.setPasswordHash(user.passwordHash());
		return entity;
	}

	static User toDomain(UserEntity entity) {
		return new User(entity.id(), entity.username(), entity.passwordHash());
	}

}
