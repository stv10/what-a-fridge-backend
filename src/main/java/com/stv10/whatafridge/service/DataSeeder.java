package com.stv10.whatafridge.service;

import com.stv10.whatafridge.domain.User;
import com.stv10.whatafridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMealService userMealService;
    
    @Value("${admin.username}")
    private String adminUsername;
    
    @Value("${admin.password}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMealService userMealService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMealService = userMealService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (userRepository.count() == 0) {
            logger.info("No users found. Seeding initial admin user.");
            User user = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .build();
            User savedUser = userRepository.save(user);
            userMealService.seedDefaultMeals(savedUser);
            logger.info("Admin user created with username: {} and default meals seeded.", adminUsername);
        } else {
            logger.info("Users already exist. Skipping seed.");
        }
    }
}
