package com.cloudread.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Table
@Entity
@Getter
@Setter
@ToString(exclude = {"books", "user"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String name;

    boolean active;

    @ManyToMany(mappedBy = "categories")
    @JsonBackReference
    List<Book> books;

    @ManyToOne
    @JsonBackReference
    Users user;
}
