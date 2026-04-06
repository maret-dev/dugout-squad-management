package com.dugout.service;

import com.dugout.model.*;
import com.dugout.repository.AttendanceRepository;
import com.dugout.repository.EventRepository;
import com.dugout.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final PlayerRepository playerRepository;
    private final TeamService teamService;

    public List<Event> getEventsForTeam(Long teamId) {
        teamService.getTeamForCurrentCoach(teamId);
        return eventRepository.findByTeamIdOrderByDateAsc(teamId);
    }

    public Event addEvent(Long teamId, EventType type, LocalDate date, String notes) {
        Team team = teamService.getTeamForCurrentCoach(teamId);

        Event event = Event.builder()
                .type(type)
                .date(date)
                .notes(notes)
                .team(team)
                .build();
        event = eventRepository.save(event);

        // Create attendance records for all active players (default absent)
        List<Player> activePlayers = playerRepository.findByTeamIdAndActiveTrue(teamId);
        for (Player player : activePlayers) {
            Attendance attendance = Attendance.builder()
                    .player(player)
                    .event(event)
                    .present(false)
                    .build();
            attendanceRepository.save(attendance);
        }

        return event;
    }

    public void deleteEvent(Long teamId, Long eventId) {
        teamService.getTeamForCurrentCoach(teamId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        eventRepository.delete(event);
    }
}
