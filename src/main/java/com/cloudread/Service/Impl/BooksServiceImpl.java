package com.cloudread.Service.Impl;

import com.cloudread.DTO.Request.Book.*;
import com.cloudread.DTO.Response.Book.BookResponse;
import com.cloudread.Entity.Authors;
import com.cloudread.Entity.Book;
import com.cloudread.Entity.Categories;
import com.cloudread.Entity.Users;
import com.cloudread.Enum.Role;
import com.cloudread.Exception.AppException;
import com.cloudread.Exception.ErrorCode;
import com.cloudread.Mapper.BookMapper;
import com.cloudread.Repository.*;
import com.cloudread.Service.BooksService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BooksServiceImpl implements BooksService {
    AuthorsRepository authorsRepository;
    UserRepository userRepository;
    CategoriesRepository categoriesRepository;
    BookRepository bookRepository;
    BookMapper bookMapper;
    AwsS3Service awsS3Service;

    public BookResponse getBookById(int id) {
        Book book = findBookById(id);
        if (book == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        if (!book.isActive()) {
            Users userLogin = getUserBySecurityContext();
            if (userLogin == null || userLogin.getRole() != Role.ROLE_ADMIN) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        return bookMapper.toResponse(book);
    }

    public List<BookResponse> getAllBooks() {
        Users userLogin = getUserBySecurityContext();
        if (userLogin == null || userLogin.getRole() == Role.ROLE_ADMIN) {
            return bookRepository.findAll().stream().map(bookMapper::toResponse).collect(Collectors.toList());
        } else {
            return bookRepository.findByActiveTrue().stream().map(bookMapper::toResponse).collect(Collectors.toList());
        }
    }
    
    public List<BookResponse> getAllMyFavorites() {
        Users user = getUserBySecurityContext();
        if (user == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        List<Book> favorites = user.getFavorites();
        if (favorites == null) {
            favorites = new ArrayList<>();
        }
        return favorites.stream().map(bookMapper::toResponse).toList();
    }

    public List<BookResponse> getAllByAuthor(int authorId) {
        Authors authors = findAuthorsById(authorId);
        if (authors == null) {
            throw new AppException(ErrorCode.AUTHOR_NOT_FOUND);
        }

        if (!authors.isActive()) {
            Users userLogin = getUserBySecurityContext();
            if (userLogin == null || userLogin.getRole() != Role.ROLE_ADMIN) {
                throw new AppException(ErrorCode.AUTHOR_NOT_FOUND);
            }
        }

        return bookRepository.findByAuthor(authors).stream().map(bookMapper::toResponse).collect(Collectors.toList());
    }

    public List<BookResponse> searchByKeyword(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword).stream().map(bookMapper::toResponse).collect(Collectors.toList());
    }

    public List<BookResponse> getAllByCategory(int categoryId) {
        Categories categories = categoriesRepository.findById(categoryId).orElse(null);
        if (categories == null) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        if (!categories.isActive()) {
            Users userLogin = getUserBySecurityContext();
            if (userLogin == null || userLogin.getRole() != Role.ROLE_ADMIN) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        return bookRepository.findByCategoriesContaining(categories).stream().map(bookMapper::toResponse).collect(Collectors.toList());
    }

    public List<BookResponse> getNewestBooks() {
        return bookRepository.findByOrderByCreatedAtDesc().stream().map(bookMapper::toResponse).collect(Collectors.toList());
    }

    public List<BookResponse> getMostFavoriteBooks() {
        return bookRepository.findTopByOrderByFavoriteDesc().stream().map(bookMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BookResponse createBook(BookCreateRequest request, MultipartFile coverImage, MultipartFile file) {
        Book book = bookMapper.toBook(request);

        List<ErrorCode> errors = new ArrayList<>();

        // Set author
        Authors authors = findAuthorsById(request.getAuthorId());
        if (authors == null) {
            errors.add(ErrorCode.AUTHOR_NOT_FOUND);
        }
        book.setAuthor(authors);

        // Verify book exists or not
        if (findBookByTitleAndAuthor(request.getTitle(), authors) != null) {
            errors.add(ErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        // Set categories
        List<Categories> categories = findCategoriesById(request.getCategoryIds());
        if (categories.size() != request.getCategoryIds().size()) {
            errors.add(ErrorCode.CATEGORY_NOT_FOUND);
        }
        book.setCategories(categories);

        // Upload file
        try {
            if (coverImage != null) {
                String coverImageName = awsS3Service.uploadFileWithCustomPrefix(coverImage, "image");
                String url = awsS3Service.getPublicUrl(coverImageName);
                book.setCoverImage(url);
            } else {
                errors.add(ErrorCode.MISSING_IMAGE);
            }

            if (file != null) {
                String fileName = awsS3Service.uploadFileWithCustomPrefix(file, "book");
                String fileUrl = awsS3Service.getPublicUrl(fileName);
                book.setFileName(fileUrl);
            } else {
                errors.add(ErrorCode.MISSING_DOCUMENT);
            }

        } catch (IOException e) {
            errors.add(ErrorCode.ERROR_UPLOAD_FILE);
        }

        book.setActive(true);

        if (errors.isEmpty()) {
            // Save
            bookRepository.save(book);
            return bookMapper.toResponse(book);
        } else {
            throw new AppException(errors);
        }
    }

    @Transactional
    public BookResponse updateBook(BookUpdateRequest request, int id, MultipartFile coverImage, MultipartFile file) {
        Book book = findBookById(id);
        if (book == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        bookMapper.updateBook(book, request);

        List<ErrorCode> errors = new ArrayList<>();

        // Set author
        Authors authors = findAuthorsById(request.getAuthorId());
        if (authors == null) {
            errors.add(ErrorCode.AUTHOR_NOT_FOUND);
        }
        book.setAuthor(authors);

        // Set categories
        List<Categories> categories = findCategoriesById(request.getCategoryIds());
        if (categories.size() != request.getCategoryIds().size()) {
            errors.add(ErrorCode.CATEGORY_NOT_FOUND);
        }
        book.setCategories(categories);

        // Upload file
        try {
            if (coverImage != null) {
                String oldCoverImage = book.getCoverImage();
                // Delete old file
                boolean result = awsS3Service.deleteByPublicUrl(oldCoverImage);
                if (!result) {
                    errors.add(ErrorCode.ERROR_DELETE_FILE);
                }


                // Upload new file
                String coverImageName = awsS3Service.uploadFileWithCustomPrefix(coverImage, "image");
                String fileUrl = awsS3Service.getPublicUrl(coverImageName);
                book.setCoverImage(fileUrl);
            }

            if (file != null) {
                String oldFileName = book.getFileName();
                // Delete old file
                boolean result = awsS3Service.deleteByPublicUrl(oldFileName);
                if (!result) {
                    errors.add(ErrorCode.ERROR_DELETE_FILE);
                }
                // Upload new file
                String fileName = awsS3Service.uploadFileWithCustomPrefix(file, "book");
                String fileUrl = awsS3Service.getPublicUrl(fileName);
                book.setFileName(fileUrl);
            }

        } catch (IOException e) {
            errors.add(ErrorCode.ERROR_UPLOAD_FILE);
        }



        if (errors.isEmpty()) {
            // Save
            bookRepository.save(book);

            BookResponse response = bookMapper.toResponse(book);

            String imgUrl= awsS3Service.getPublicUrl(book.getCoverImage());
            response.setCoverUrl(imgUrl);
            String fileUrl= awsS3Service.getPublicUrl(book.getFileName());
            response.setFileUrl(fileUrl);

            return response;
        } else {
            throw new AppException(errors);
        }
    }

    @Transactional
    public void deleteBookById(int id) {
        Book book = findBookById(id);

        if (book == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        } else {
            String fileName = book.getFileName();
            String coverImageName = book.getCoverImage();
            // Delete remote file
            try {
                awsS3Service.deleteFile(fileName);
                awsS3Service.deleteFile(coverImageName);
            } catch (IOException e) {
                throw new AppException(ErrorCode.ERROR_DELETE_FILE);
            }
        }
        bookRepository.delete(book);
    }

    @Transactional
    public void changeBookStatus(int id) {
        Book book = findBookById(id);
        if (book == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        book.setActive(!book.isActive());
        bookRepository.save(book);
    }

    @Transactional
    public void markFavorite(int id) {
        Users user = getUserBySecurityContext();
        if (user == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        List<Book> favorites = user.getFavorites();
        if (favorites == null) {
            favorites = new ArrayList<>();
        }
        Book book = findBookById(id);
        
        if (book == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        } else {
            if (!book.isActive()) {
                Users userLogin = getUserBySecurityContext();
                if (userLogin == null || userLogin.getRole() != Role.ROLE_ADMIN) {
                    throw new AppException(ErrorCode.NOT_FOUND);
                }
            } else {
                if (favorites.contains(book)) {
                    favorites.remove(book);
                    book.setFavorite(book.getFavorite() - 1);
                    bookRepository.save(book);
                } else {
                    favorites.add(book);
                    book.setFavorite(book.getFavorite() + 1);
                    bookRepository.save(book);
                }
                user.setFavorites(favorites);
                userRepository.save(user);
            }
        }
    }

    /** Utils method */
    private Book findBookById(int id) {
        return bookRepository.findById(id).orElse(null);
    }

    private Book findBookByTitleAndAuthor(String title, Authors author) {
        return bookRepository.findByTitleAndAuthor(title, author).orElse(null);
    }

    private Authors findAuthorsById(int id) {
        return authorsRepository.findById(id).orElse(null);
    }

    private List<Categories> findCategoriesById(List<Integer> ids) {
        return categoriesRepository.findAllById(ids);
    }

    private Users getUserBySecurityContext() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(userId).orElse(null);
    }



}
