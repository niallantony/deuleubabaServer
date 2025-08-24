package com.niallantony.deulaubaba;

import com.niallantony.deulaubaba.data.RoleRepository;
import com.niallantony.deulaubaba.domain.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DeulaubabaApplication {

	@Bean
	CommandLineRunner seedRoles(RoleRepository roleRepository) {
		return args -> {
			String roleName = "ROLE_USER";

			roleRepository.findByName(roleName).ifPresentOrElse(
					r -> System.out.println("Role Already Exists" + roleName),
					() -> {
						Role role = new Role(roleName);
						roleRepository.save(role);
						System.out.println("Role Saved" + roleName);
					}
			);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(DeulaubabaApplication.class, args);
	}

}
