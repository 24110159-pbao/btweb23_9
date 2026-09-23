package vn.iotstar.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        Role userRole = roleRepository
                .findByName("ROLE_USER")
                .orElseGet(() ->
                        roleRepository.save(
                                new Role("ROLE_USER")
                        )
                );

        Role adminRole = roleRepository
                .findByName("ROLE_ADMIN")
                .orElseGet(() ->
                        roleRepository.save(
                                new Role("ROLE_ADMIN")
                        )
                );

        if (!userRepository.existsByUsername("admin")) {

            User admin = new User();

            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(
                    passwordEncoder.encode("123456")
            );
            admin.setFullName("Administrator");
            admin.setRole(adminRole);
            admin.setEnabled(true);

            userRepository.save(admin);
        }
    }
}
