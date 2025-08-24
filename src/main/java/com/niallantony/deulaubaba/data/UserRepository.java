package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
