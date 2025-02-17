package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResultDto {
    private List<RequestDto> confirmedRequests;
    private List<RequestDto> rejectedRequests;
}