package com.dugout.service;

import com.dugout.dto.PlayerStatsDto;
import com.dugout.dto.TeamStatsDto;
import com.dugout.model.*;
import com.dugout.repository.AttendanceRepository;
import com.dugout.repository.EventRepository;
import com.dugout.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final PlayerRepository playerRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;

    public TeamStatsDto getTeamStats(Long teamId) {
        List<Player> players = playerRepository.findByTeamIdAndActiveTrue(teamId);
        List<Event> events = eventRepository.findByTeamIdOrderByDateAsc(teamId);
        List<Attendance> allAttendances = attendanceRepository.findByEvent_TeamId(teamId);

        // Partition events by type
        List<Event> trainings = events.stream()
                .filter(e -> e.getType() == EventType.TRAINING)
                .collect(Collectors.toList());
        List<Event> matches = events.stream()
                .filter(e -> e.getType() == EventType.MATCH)
                .collect(Collectors.toList());

        // Build lookup: playerId -> eventId -> present
        Map<Long, Map<Long, Boolean>> lookup = new HashMap<>();
        for (Attendance a : allAttendances) {
            lookup.computeIfAbsent(a.getPlayer().getId(), k -> new HashMap<>())
                    .put(a.getEvent().getId(), a.isPresent());
        }

        // Last 3 training events (by date desc) for low attendance check
        List<Event> last3Trainings = trainings.stream()
                .sorted(Comparator.comparing(Event::getDate).reversed())
                .limit(3)
                .collect(Collectors.toList());

        List<PlayerStatsDto> playerStats = new ArrayList<>();
        for (Player player : players) {
            Map<Long, Boolean> playerAttendance = lookup.getOrDefault(player.getId(), new HashMap<>());

            int trainingPresent = countPresent(playerAttendance, trainings);
            int matchPresent = countPresent(playerAttendance, matches);

            // Low attendance: if >= 3 total trainings, check last 3
            boolean lowAttendance = false;
            if (trainings.size() >= 3) {
                int last3Count = countPresent(playerAttendance, last3Trainings);
                lowAttendance = last3Count <= 1;
            }

            PlayerStatsDto stats = PlayerStatsDto.builder()
                    .playerId(player.getId())
                    .playerName(player.getLastName() + " " + player.getFirstName())
                    .trainingPresent(trainingPresent)
                    .trainingTotal(trainings.size())
                    .trainingPercentage(percentage(trainingPresent, trainings.size()))
                    .matchPresent(matchPresent)
                    .matchTotal(matches.size())
                    .matchPercentage(percentage(matchPresent, matches.size()))
                    .lowAttendanceFlag(lowAttendance)
                    .build();
            playerStats.add(stats);
        }

        // Top 3 by training attendance count
        List<PlayerStatsDto> top3Training = playerStats.stream()
                .sorted(Comparator.comparingInt(PlayerStatsDto::getTrainingPresent).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // Top 3 by match attendance count
        List<PlayerStatsDto> top3Match = playerStats.stream()
                .sorted(Comparator.comparingInt(PlayerStatsDto::getMatchPresent).reversed())
                .limit(3)
                .collect(Collectors.toList());

        return TeamStatsDto.builder()
                .playerStats(playerStats)
                .top3Training(top3Training)
                .top3Match(top3Match)
                .build();
    }

    private int countPresent(Map<Long, Boolean> playerAttendance, List<Event> events) {
        int count = 0;
        for (Event event : events) {
            if (Boolean.TRUE.equals(playerAttendance.get(event.getId()))) {
                count++;
            }
        }
        return count;
    }

    private double percentage(int present, int total) {
        if (total == 0) return 0.0;
        return Math.round((double) present / total * 1000.0) / 10.0;
    }
}
