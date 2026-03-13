package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.entity.User;
import com.restaurant.ordering.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOpenid(String openid);

    Optional<User> findByPhone(String phone);

    boolean existsByOpenid(String openid);

    boolean existsByPhone(String phone);

    long countByStatus(UserStatus status);
}