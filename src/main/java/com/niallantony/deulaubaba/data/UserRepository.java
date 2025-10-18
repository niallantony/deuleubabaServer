package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByUsername(String username);

    @Query(
            "SELECT u FROM User u WHERE u.userId IN :ids"
    )
    List<User> findByIds(List<String> ids);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query(
            "SELECT u FROM User u WHERE u.username IN :usernames"
    )
    Set<User> findAllByUsernames(@Param("usernames") Set<String> usernames);
}
