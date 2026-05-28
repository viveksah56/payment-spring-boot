package com.backend.Seeder;
import com.backend.Entity.Role;
import com.backend.Enum.RoleType;
import com.backend.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Arrays.stream(RoleType.values()).forEach(roleType -> {
            if (roleRepository.findByName(roleType).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(roleType)
                        .description(roleType.name() + " role")
                        .build());
                log.info("Seeded role: {}", roleType);
            }
        });
    }
}