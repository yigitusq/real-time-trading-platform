package com.yigitusq.orderservice.config;

import com.yigitusq.orderservice.entity.User;
import com.yigitusq.orderservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User testUser = new User();
            testUser.setUsername("Yigit");
            testUser.setBalance(new BigDecimal("10000.00"));
            userRepository.save(testUser);
            System.out.println("Sisteme test kullanıcısı ve 10.000$ bakiye tanımlandı.");
        }
    }
}
