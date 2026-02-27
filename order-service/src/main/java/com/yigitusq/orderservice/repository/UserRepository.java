package com.yigitusq.orderservice.repository;

import com.yigitusq.orderservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
