package ru.practicum.request;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.RequestDto;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RequestController {
    private final RequestService participationRequestService;

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createParticipationRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam @Positive Long eventId) {
        return participationRequestService.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public RequestDto cancelParticipationRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return participationRequestService.cancelParticipationRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/requests")
    public List<RequestDto> getParticipationRequests(@PathVariable @Positive Long userId) {
        return participationRequestService.getParticipationRequests(userId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<RequestDto> getParticipationRequestsForUserEvent(@PathVariable @Positive Long userId,
                                                                 @PathVariable @Positive Long eventId) {
        return participationRequestService.getParticipationRequestsForUserEvent(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResultDto changeParticipationRequestsStatus(@PathVariable @Positive Long userId,
                                                                               @PathVariable @Positive Long eventId,
                                                                               @RequestBody EventRequestStatusUpdateRequestDto eventRequestStatusUpdateRequest) {
        return participationRequestService.changeParticipationRequestsStatus(userId, eventId, eventRequestStatusUpdateRequest);
    }
}