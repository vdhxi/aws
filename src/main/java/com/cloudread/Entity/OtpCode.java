package com.cloudread.Entity;

import com.cloudread.Enum.TokenScope;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "email")
    String email;

    @Column(name = "otp")
    String otp;

    TokenScope scope;

    @CreationTimestamp
    LocalDateTime createdAt;

    LocalDateTime expiresAt;
}
