package com.dugout.dto;

import com.dugout.model.Event;
import com.dugout.model.Player;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AttendanceGridDto {

    private List<Event> events;
    private List<PlayerRow> playerRows;

    @Data
    @Builder
    public static class PlayerRow {
        private Player player;
        private Map<Long, Boolean> attendanceMap; // eventId -> present
    }
}
