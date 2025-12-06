package com.cloudread.Repository;

import com.cloudread.Entity.Authors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorsRepository extends JpaRepository<Authors, Integer> {
    List<Authors> findByActive(Boolean active);
    List<Authors> findByNameContainingIgnoreCase(String name);
    Optional<Authors> findByName(String name);
}
