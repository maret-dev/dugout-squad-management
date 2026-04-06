package com.dugout.service;

import com.dugout.dto.AttendanceGridDto;
import com.dugout.model.Attendance;
import com.dugout.model.Event;
import com.dugout.model.Player;
import com.dugout.repository.AttendanceRepository;
import com.dugout.repository.EventRepository;
import com.dugout.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final PlayerRepository playerRepository;
    private final EventRepository eventRepository;
    private final TeamService teamService;

    public AttendanceGridDto getAttendanceGrid(Long teamId) {
        teamService.getTeamForCurrentCoach(teamId);

        List<Event> events = eventRepository.findByTeamIdOrderByDateAsc(teamId);
        List<Player> players = playerRepository.findByTeamIdAndActiveTrue(teamId);
        List<Attendance> allAttendances = attendanceRepository.findByEvent_TeamId(teamId);

        // Build a lookup: playerId -> (eventId -> present)
        Map<Long, Map<Long, Boolean>> lookup = new HashMap<>();
        for (Attendance a : allAttendances) {
            lookup.computeIfAbsent(a.getPlayer().getId(), k -> new HashMap<>())
                    .put(a.getEvent().getId(), a.isPresent());
        }

        List<AttendanceGridDto.PlayerRow> rows = players.stream()
                .map(player -> AttendanceGridDto.PlayerRow.builder()
                        .player(player)
                        .attendanceMap(lookup.getOrDefault(player.getId(), new HashMap<>()))
                        .build())
                .collect(Collectors.toList());

        return AttendanceGridDto.builder()
                .events(events)
                .playerRows(rows)
                .build();
    }

    public void saveAttendance(Long teamId, Map<String, Boolean> attendanceData) {
        teamService.getTeamForCurrentCoach(teamId);

        for (Map.Entry<String, Boolean> entry : attendanceData.entrySet()) {
            String[] parts = entry.getKey().split("_");
            if (parts.length != 2) continue;

            Long playerId = Long.parseLong(parts[0]);
            Long eventId = Long.parseLong(parts[1]);
            boolean present = entry.getValue();

            attendanceRepository.findByPlayerIdAndEventId(playerId, eventId)
                    .ifPresent(attendance -> {
                        attendance.setPresent(present);
                        attendanceRepository.save(attendance);
                    });
        }
    }
}
