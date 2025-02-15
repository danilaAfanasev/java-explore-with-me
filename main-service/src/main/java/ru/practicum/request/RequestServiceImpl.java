package ru.practicum.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.exceptions.EventNotFoundException;
import ru.practicum.exceptions.ForbiddenException;
import ru.practicum.exceptions.UserNotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.request.dto.RequestMapper.toParticipationRequestDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public RequestDto createParticipationRequest(Long userId, Long eventId) {
        log.info("Добавление запроса от текущего пользователя на участие в мероприятии: user_id = " + userId + ", event_id = " + eventId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        Request existParticipationRequest = participationRequestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (existParticipationRequest != null) {
            log.info("Ошибка: Пользователь с ID = " + userId + " не может добавить повторный запрос с ID = " + eventId);
            throw new ForbiddenException("Could not add the same request.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            log.info("Ошибка: инициатор с ID = " + userId + " не может добавить запрос на участие в его мероприятии с ID = " + eventId);
            throw new ForbiddenException("Initiator could not add request to own event.");
        }
        if (event.getState() != EventState.PUBLISHED) {
            log.info("Ошибка: Пользователь с ID = " + userId + " не может принять участие в неопубликованном мероприятии с ID = " + eventId);
            throw new ForbiddenException("Could not participate in non-published event.");
        }
        if (event.getParticipantLimit() != 0 && participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            log.info("Ошибка: Пользователь с ID = " + userId + " не может участвовать в мероприятии с ID = " + eventId + ", с тех пор, как был достигнут лимит заявок на участие");
            throw new ForbiddenException("Достигнут лимит участников.");
        }
        RequestStatus status = RequestStatus.PENDING;
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        }
        Request newParticipationRequest = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .status(status)
                .build();
        return toParticipationRequestDto(participationRequestRepository.save(newParticipationRequest));
    }

    @Override
    public RequestDto cancelParticipationRequest(Long userId, Long requestId) {
        log.info("Отмена вашего запроса на участие в мероприятии: user_id = " + userId + ", request_id = " + requestId);
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Request requestToUpdate = participationRequestRepository.getReferenceById(requestId);
        requestToUpdate.setStatus(RequestStatus.CANCELED);
        return toParticipationRequestDto(participationRequestRepository.save(requestToUpdate));
    }

    @Override
    public List<RequestDto> getParticipationRequests(Long userId) {
        log.info("Получение информации о запросах текущего пользователя на участие в мероприятиях других людей: user_id = " + userId);
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        List<Optional<Request>> requests = participationRequestRepository.findByRequesterId(userId);
        return requests.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getParticipationRequestsForUserEvent(Long userId, Long eventId) {
        log.info("Получение информации о заявках на участие в мероприятии текущего пользователя: user_id = " + userId +
                ", event_id = " + eventId);
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        List<Event> userEvents = eventRepository.findByIdAndInitiatorId(eventId, userId);
        List<Optional<Request>> requests = participationRequestRepository.findByEventIn(userEvents);
        return requests.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto changeParticipationRequestsStatus(Long userId, Long eventId,
                                                                               EventRequestStatusUpdateRequestDto eventRequestStatusUpdateRequest) {
        log.info("Изменение статуса (confirmed, canceled) заявок на участие в мероприятии текущего пользователя: " +
                "user_id = " + userId + ", event_id = " + eventId + ", новый статус = " + eventRequestStatusUpdateRequest);
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        List<Request> requests = participationRequestRepository.findAllById(eventRequestStatusUpdateRequest.getRequestIds());
        EventRequestStatusUpdateResultDto eventRequestStatusUpdateResultDto = EventRequestStatusUpdateResultDto.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();
        if (!requests.isEmpty()) {
            if (RequestStatus.valueOf(eventRequestStatusUpdateRequest.getStatus()) == RequestStatus.CONFIRMED) {
                int limitParticipants = event.getParticipantLimit();
                if (limitParticipants == 0 || !event.isRequestModeration()) {
                    throw new ForbiddenException("Не требуется принимать запросы, поскольку лимит участников равен 0 или " +
                            "pre-moderation off");
                }
                Integer countParticipants = participationRequestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
                if (countParticipants == limitParticipants) {
                    throw new ForbiddenException("Достигнут лимит участников");
                }
                for (Request request : requests) {
                    if (request.getStatus() != RequestStatus.PENDING) {
                        throw new ForbiddenException("Статус запроса не PENDING");
                    }
                    if (countParticipants < limitParticipants) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        eventRequestStatusUpdateResultDto.getConfirmedRequests().add(toParticipationRequestDto(request));
                        countParticipants++;
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        eventRequestStatusUpdateResultDto.getRejectedRequests().add(toParticipationRequestDto(request));
                    }
                }
                participationRequestRepository.saveAll(requests);
                if (countParticipants == limitParticipants) {
                    participationRequestRepository.updateRequestStatusByEventIdAndStatus(event,
                            RequestStatus.PENDING, RequestStatus.REJECTED);
                }
            } else if (RequestStatus.valueOf(eventRequestStatusUpdateRequest.getStatus()) == RequestStatus.REJECTED) {
                for (Request request : requests) {
                    if (request.getStatus() != RequestStatus.PENDING) {
                        throw new ForbiddenException("Статус запроса не PENDING");
                    }
                    request.setStatus(RequestStatus.REJECTED);
                    eventRequestStatusUpdateResultDto.getRejectedRequests().add(toParticipationRequestDto(request));
                }
                participationRequestRepository.saveAll(requests);
            }
        }
        return eventRequestStatusUpdateResultDto;
    }
}