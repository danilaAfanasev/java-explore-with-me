package ru.practicum.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.category.CategoryController;
import ru.practicum.comment.CommentController;
import ru.practicum.compilation.CompilationController;
import ru.practicum.event.EventController;
import ru.practicum.request.RequestController;
import ru.practicum.user.UserController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice(assignableTypes = {
        UserController.class,
        CategoryController.class,
        EventController.class,
        RequestController.class,
        CompilationController.class,
        CommentController.class})
public class ErrorHandler {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ApiError("BAD_REQUEST", "Некорректно составленный запрос.",
                e.getMessage(), LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    }

    @ExceptionHandler(ValidationRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationRequestException(ValidationRequestException e) {
        return new ApiError("BAD_REQUEST", "Ошибка валидации данных.",
                e.getMessage(), LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            CategoryNotFoundException.class,
            EventNotFoundException.class,
            RequestNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleObjectNotFoundException(EntityNotFoundException e) {
        return new ApiError("NOT_FOUND", "Запрашиваемый объект не найден.",
                e.getMessage(), LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    }

    @ExceptionHandler({ConstraintViolationException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleUniqueConstraintViolation(Exception e) {
        return new ApiError("CONFLICT", "Нарушение уникальности данных (дубликат).",
                e.getMessage(), LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleForbiddenOperationsException(ForbiddenException e) {
        return new ApiError("CONFLICT", "Запрошенная операция невозможна.",
                e.getMessage(), LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    }

    @ExceptionHandler(RequestConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleRequestConflictException(RequestConflictException e) {
        return new ApiError("CONFLICT", "Конфликт запроса на участие.",
                e.getMessage(), LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    }
}
