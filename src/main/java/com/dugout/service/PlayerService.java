package com.dugout.service;

import com.dugout.model.Attendance;
import com.dugout.model.Event;
import com.dugout.model.Player;
import com.dugout.model.Team;
import com.dugout.repository.AttendanceRepository;
import com.dugout.repository.EventRepository;
import com.dugout.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final TeamService teamService;

    public List<Player> getActivePlayers(Long teamId) {
        teamService.getTeamForCurrentCoach(teamId);
        return playerRepository.findByTeamIdAndActiveTrue(teamId);
    }

    public Player addPlayer(Long teamId, String firstName, String lastName) {
        Team team = teamService.getTeamForCurrentCoach(teamId);
        Player player = Player.builder()
                .firstName(firstName)
                .lastName(lastName)
                .team(team)
                .active(true)
                .build();
        player = playerRepository.save(player);

        // Create attendance records for all existing events (default absent)
        List<Event> events = eventRepository.findByTeamIdOrderByDateAsc(teamId);
        for (Event event : events) {
            Attendance attendance = Attendance.builder()
                    .player(player)
                    .event(event)
                    .present(false)
                    .build();
            attendanceRepository.save(attendance);
        }

        return player;
    }

    public int importPlayersFromCsv(Long teamId, InputStream csvStream) throws IOException {
        Team team = teamService.getTeamForCurrentCoach(teamId);
        List<Event> events = eventRepository.findByTeamIdOrderByDateAsc(teamId);
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;
                String firstName = parts[0].trim();
                String lastName = parts[1].trim();
                if (firstName.isEmpty() || lastName.isEmpty()) continue;
                Player player = Player.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .team(team)
                        .active(true)
                        .build();
                player = playerRepository.save(player);
                for (Event event : events) {
                    Attendance attendance = Attendance.builder()
                            .player(player)
                            .event(event)
                            .present(false)
                            .build();
                    attendanceRepository.save(attendance);
                }
                count++;
            }
        }
        return count;
    }

    public void removePlayer(Long teamId, Long playerId) {
        teamService.getTeamForCurrentCoach(teamId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        playerRepository.delete(player);
    }
}
