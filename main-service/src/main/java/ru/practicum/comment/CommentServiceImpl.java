package ru.practicum.comment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentMapper;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.exceptions.CommentNotFoundException;
import ru.practicum.exceptions.EventNotFoundException;
import ru.practicum.exceptions.ForbiddenException;
import ru.practicum.exceptions.UserNotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.comment.dto.CommentMapper.toComment;
import static ru.practicum.comment.dto.CommentMapper.toCommentResponseDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentResponseDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.info("Создан комментарий с  ID = " + userId + ", для события с ID = " + eventId + ": " + newCommentDto);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        Comment comment = toComment(newCommentDto);
        comment.setEvent(event);
        comment.setAuthor(user);
        comment.setState(CommentState.PENDING);
        comment.setCreatedOn(LocalDateTime.now());
        return toCommentResponseDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentResponseDto> getEventComments(Long eventId, int from, int size) {
        log.info("Получить комментарии события с ID = " + eventId);
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        List<Comment> comments = commentRepository.findByEvent(event, PageRequest.of(from / size, size));
        return comments.stream().map(CommentMapper::toCommentResponseDto).collect(Collectors.toList());
    }

    @Override
    public CommentResponseDto getCommentById(Long commentId) {
        log.info("Получить комментарий с ID = " + commentId);
        return toCommentResponseDto(commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId)));
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        log.info("Обновить комментарий с ID = " + commentId + " от пользователя с ID = " + userId + ": " + newCommentDto);
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Нельзя обновить комментарий другого пользователя.");
        }
        if (comment.getState() == CommentState.CONFIRMED) {
            throw new ForbiddenException("Нельзя обновить готовый комментарий.");
        }
        comment.setText(newCommentDto.getText());
        comment.setUpdatedOn(LocalDateTime.now());
        comment.setState(CommentState.PENDING);
        return toCommentResponseDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("Удалить комментарий с ID = " + commentId + " от пользователя с ID = " + userId);
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Нельзя обновить комментарий другого пользователя.");
        }
        if (comment.getState() == CommentState.CONFIRMED) {
            throw new ForbiddenException("Нельзя удалить готовый комментарий.");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public CommentResponseDto updateCommentStatusByAdmin(Long commentId, boolean isConfirm) {
        log.info("Confirm/reject комментарий с ID = " + commentId + ". New state: " + isConfirm);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (isConfirm) {
            comment.setState(CommentState.CONFIRMED);
        } else {
            comment.setState(CommentState.REJECTED);
        }
        comment.setPublishedOn(LocalDateTime.now());
        return toCommentResponseDto(commentRepository.save(comment));
    }
}