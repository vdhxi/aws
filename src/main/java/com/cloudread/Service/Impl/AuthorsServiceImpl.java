package com.cloudread.Service.Impl;

import com.cloudread.DTO.Request.Author.AuthorCreateRequest;
import com.cloudread.DTO.Request.Author.AuthorUpdateRequest;
import com.cloudread.DTO.Response.Author.AuthorResponse;
import com.cloudread.Entity.Authors;
import com.cloudread.Entity.Book;
import com.cloudread.Entity.Users;
import com.cloudread.Enum.Role;
import com.cloudread.Exception.AppException;
import com.cloudread.Exception.ErrorCode;
import com.cloudread.Mapper.AuthorsMapper;
import com.cloudread.Repository.AuthorsRepository;
import com.cloudread.Repository.BookRepository;
import com.cloudread.Repository.UserRepository;
import com.cloudread.Service.AuthorsService;
import com.cloudread.Service.AwsS3Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorsServiceImpl implements AuthorsService {
    AuthorsRepository authorsRepository;
    AuthorsMapper authorsMapper;
    BookRepository bookRepository;
    AwsS3Service awsS3Service;
    UserRepository userRepository;

    public AuthorResponse getAuthorById(int id) {
        Authors author = findById(id);

        if (!author.isActive()) {
            Users userLogin = getUserBySecurityContextHolder();
            if (userLogin.getRole() != Role.ROLE_ADMIN) {
                throw new AppException(ErrorCode.NOT_FOUND);
            }
        }

        AuthorResponse response = authorsMapper.toResponse(author);

        String imageUrl = awsS3Service.getPublicUrl(author.getImage());
        response.setImageUrl(imageUrl);

        return response;
    }

    public List<AuthorResponse> getAllAuthors() {
        List<Authors> authors = authorsRepository.findAll();
        List<AuthorResponse> authorResponseList = new ArrayList<>();
        authors.forEach(author -> {
            AuthorResponse response = authorsMapper.toResponse(author);
            String imageUrl = awsS3Service.getPublicUrl(author.getImage());
            response.setImageUrl(imageUrl);
            authorResponseList.add(response);
        });
        return authorResponseList;
    }

    public List<AuthorResponse> getAllActiveAuthors() {
        List<Authors> authors = authorsRepository.findByActive(true);
        List<AuthorResponse> authorResponseList = new ArrayList<>();
        authors.forEach(author -> {
            AuthorResponse response = authorsMapper.toResponse(author);
            String imageUrl = awsS3Service.getPublicUrl(author.getImage());
            response.setImageUrl(imageUrl);
            authorResponseList.add(response);
        });
        return authorResponseList;
    }

    public AuthorResponse createAuthor(AuthorCreateRequest request, MultipartFile image) {
        Authors author = authorsMapper.toAuthor(request);

        if (findByName(request.getName()) != null) {
            throw new AppException(ErrorCode.AUTHOR_ALREADY_EXISTS);
        }

        try {
            String imageUrl = awsS3Service.uploadFile(image);
            author.setImage(imageUrl);
        } catch (Exception e) {
            throw new AppException(ErrorCode.ERROR_UPLOAD_FILE);
        }

        author.setActive(true);

        // Save
        authorsRepository.save(author);

        AuthorResponse response = authorsMapper.toResponse(author);

        String imageUrl = awsS3Service.getPublicUrl(author.getImage());
        response.setImageUrl(imageUrl);

        return response;
    }

    public AuthorResponse updateAuthor(AuthorUpdateRequest request, int id, MultipartFile image) {
        Authors author = findById(id);


        if (findByName(request.getName()) != null && !author.getName().equalsIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.AUTHOR_ALREADY_EXISTS);
        }

        authorsMapper.updateAuthor(author, request);

        try {
            if (image != null) {
                try {
                    awsS3Service.deleteFile(author.getImage());
                } catch (Exception e) {
                    throw  new AppException(ErrorCode.ERROR_DELETE_FILE);
                }
                String imageUrl = awsS3Service.uploadFile(image);
                author.setImage(imageUrl);
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.ERROR_UPLOAD_FILE);
        }

        // Save
        authorsRepository.save(author);

        AuthorResponse response = authorsMapper.toResponse(author);
        String imageUrl = awsS3Service.getPublicUrl(author.getImage());
        response.setImageUrl(imageUrl);

        return response;
    }

    public void changeStatus(int id) {
        Authors author = findById(id);
        author.setActive(!author.isActive());

        // If author is disable -> disable all books of author
        if (!author.isActive()) {
            List<Book> bookList = author.getBooks();
            bookList.forEach(singleBook -> {
                singleBook.setActive(false);
                bookRepository.save(singleBook);
            });

        }

        authorsRepository.save(author);
    }

    public void deleteAuthorById(int id) {
        Authors author = findById(id);
        authorsRepository.delete(author);
    }

    private Authors findById(int id) {
        return authorsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private Authors findByName(String name) {
        return authorsRepository.findByName(name).orElse(null);
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
}
