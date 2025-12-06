package com.cloudread.Config;

import com.cloudread.Entity.Users;
import com.cloudread.Enum.Role;
import com.cloudread.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InitialConfig {
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByRole(Role.ROLE_ADMIN).isEmpty()) {
                Users admin = Users.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .active(true)
                        .role(Role.ROLE_ADMIN)
                        .build();
                userRepository.save(admin);


                Users staff = Users.builder()
                        .username("staff")
                        .password(passwordEncoder.encode("staff"))
                        .active(true)
                        .role(Role.ROLE_USER)
                        .build();
                userRepository.save(staff);


                System.out.println("Admin has been created");
                System.out.println("Admin credentials:");
                System.out.println("username: " + admin.getUsername());
                System.out.println("password: admin");

                System.out.println("Staff has been created");
                System.out.println("Staff credentials:");
                System.out.println("username: " + staff.getUsername());
                System.out.println("password: staff");
            }
        };
    }
}
