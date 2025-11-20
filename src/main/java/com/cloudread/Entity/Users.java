package com.cloudread.Entity;

import com.cloudread.Enum.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Table
@Entity
@Getter
@Setter
@ToString(exclude = "favorites")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String username;

    String email;

    String password;

    boolean active;

    Role role;

    @ManyToMany
    @JoinTable(
            name = "UserFavoriteBooks",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "bookId")
    )
    List<Book> favorites;

}
