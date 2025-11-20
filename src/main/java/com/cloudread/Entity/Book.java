package com.cloudread.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Table
@Entity
@Getter
@Setter
@ToString(exclude = {"categories", "users"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String title;

    String titleNormal;

    String description;

    String coverImage;

    String fileName;

    int favorite;

    boolean active;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @ManyToOne
    @JsonBackReference
    Authors author;

    @ManyToMany
    @JoinTable(
            name = "BookCategories",
            joinColumns = @JoinColumn(name = "bookId"),
            inverseJoinColumns = @JoinColumn(name = "categoriesId")
    )
    @JsonManagedReference
    List<Categories> categories;

    @ManyToMany(mappedBy = "favorites")
    List<Users> users;
}
