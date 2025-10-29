package com.cloudread.Repository;

import com.cloudread.Entity.Authors;
import com.cloudread.Entity.Book;
import com.cloudread.Entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByAuthor(Authors author);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByCategoriesContaining(Categories category);
    Optional<Book> findByTitleAndAuthor(String title, Authors author);
    List<Book> findByActiveTrue();

    @Query("SELECT sab FROM Book sab ORDER BY sab.favorite DESC")
    List<Book> findTopByOrderByFavoriteDesc();
    
    @Query("SELECT sab FROM Book sab ORDER BY sab.createdAt DESC")
    List<Book> findByOrderByCreatedAtDesc();
}


