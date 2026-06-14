package com.epam.gym.crm.repository;

import com.epam.gym.crm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.username FROM User u")
    List<String> findAllUsernames();

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}