package com.cloudread.Repository;

import com.cloudread.Entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Integer> {
    List<Categories> findByActive(boolean active);
    Optional<Categories> findByName(String name);
}
