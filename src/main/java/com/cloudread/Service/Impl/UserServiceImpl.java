package com.cloudread.Service.Impl;

import com.cloudread.DTO.Request.Authenticate.ChangePasswordRequest;
import com.cloudread.DTO.Request.User.UserCreateRequest;
import com.cloudread.DTO.Request.User.UserUpdateRequest;
import com.cloudread.DTO.Response.User.UserResponse;
import com.cloudread.Entity.Users;
import com.cloudread.Enum.Role;
import com.cloudread.Exception.AppException;
import com.cloudread.Exception.ErrorCode;
import com.cloudread.Mapper.UsersMapper;
import com.cloudread.Repository.UserRepository;
import com.cloudread.Service.UserService;
import com.cloudread.Utils.MailMessage;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    UsersMapper userMapper;
    AuthenticateService authenticateService;
    MailServerService mailServerService;

    List<String> ignoredUsernameList = List.of("admin", "administrator", "user", "test", "username", "null", "void", "staff");

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    public UserResponse getUserById(String id) {

        Users userLogin = getUserBySecurityContextHolder();

        if (!userLogin.getRole().equals(Role.ROLE_ADMIN)) {
            String userLoginId = userLogin.getId();
            if (!userLoginId.equals(id)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            } else {
                return userMapper.toResponse(userLogin);
            }
        } else {
            Users user = userRepository.findById(id).orElse(null);
            if (user != null) {
                return userMapper.toResponse(user);
            } else {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
        }
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request, String token) {
        List<ErrorCode> errorList = new ArrayList<>();
        // Extract data
        String username = request.getUsername().trim();

        if (ignoredUsernameList.contains(username.toLowerCase())) {
            errorList.add(ErrorCode.USERNAME_NOT_ALLOWED);
        }

        String password = request.getPassword().trim();
        String email = extractSubjectFromToken(token);

        // Verify username
        Users userExists = userRepository.findByUsername(username).orElse(null);
        if (userExists != null) {
            errorList.add(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // Verify email
        userExists = userRepository.findByEmail(email).orElse(null);
        if (userExists != null) {
            errorList.add(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // Validate password
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            errorList.add(ErrorCode.PASSWORD_DID_NOT_MATCH);
        }

        if (!errorList.isEmpty()) {
            throw new AppException(errorList);
        }

        Users user = userMapper.toUser(request);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        // Send email
        mailServerService.sendMail(email, MailMessage.CREATE_ACCOUNT_SUBJECT, MailMessage.CREATE_ACCOUNT_MESSAGE);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UserUpdateRequest request) {
        List<ErrorCode> errorList = new ArrayList<>();

        Users users = getUserBySecurityContextHolder();

        if (!request.getUsername().isEmpty()) {
            if (getUserByUsername(request.getUsername()) != null) {
                errorList.add(ErrorCode.USERNAME_ALREADY_EXISTS);
            }
        }

        if (!errorList.isEmpty()) {
            throw new AppException(errorList);
        }

        return  userMapper.toResponse(users);
    }

    @Transactional
    public UserResponse changeEmail(String token) {
        Users user = getUserBySecurityContextHolder();
        String subjectToken = extractSubjectFromToken(token);
        String email = extractClaimsFromToken(token, "email");
        if (!user.getId().equals(subjectToken)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        } else {
            user.setEmail(email);
            userRepository.save(user);
            return userMapper.toResponse(user);
        }
    }

    @Transactional
    public void changePassword(String token, ChangePasswordRequest request) {
        String userId = extractSubjectFromToken(token);
        Users users = userRepository.findById(userId).orElse(null);
        if (users != null) {
            String password = request.getPassword();
            String confirmPassword = request.getConfirmPassword();
            if (!password.equals(confirmPassword)) {
                throw new AppException(ErrorCode.PASSWORD_DID_NOT_MATCH);
            } else {
                users.setPassword(passwordEncoder.encode(password));
                userRepository.save(users);

                mailServerService.sendMail(users.getEmail(), MailMessage.CHANGE_PASSWORD_SUCCESS_SUBJECT, MailMessage.CHANGE_PASSWORD_SUCCESS_MESSAGE);

                // Delete token after use
                authenticateService.deleteTokenAfterUse(token);
            }

        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    /** Private method */

    private String extractSubjectFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractClaimsFromToken(String token, String claim) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getClaim(claim).toString();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Users getUserBySecurityContextHolder() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        } else  {
            return user;
        }
    }

    private Users getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

}
