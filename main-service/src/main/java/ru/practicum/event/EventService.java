package ru.practicum.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.event.dto.UpdateEventUserRequestDto;

import java.util.List;

public interface EventService {

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEvents(Long userId, int from, int size);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequestDto updateEventUserRequestDto);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto);

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                        String rangeStart, String rangeEnd, Integer from, Integer size);

    List<EventShortDto> getPublishedEvents(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd,
                                           boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request);

    EventFullDto getPublishedEventById(Long eventId, HttpServletRequest request);
}