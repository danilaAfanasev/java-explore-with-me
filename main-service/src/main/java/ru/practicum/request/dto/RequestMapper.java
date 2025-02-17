package ru.practicum.request.dto;

import lombok.NoArgsConstructor;
import ru.practicum.request.Request;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor
public class RequestMapper {

    public static RequestDto toParticipationRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().toString())
                .created(request.getCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}