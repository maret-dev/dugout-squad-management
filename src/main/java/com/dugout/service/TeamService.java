package com.dugout.service;

import com.dugout.model.Coach;
import com.dugout.model.Role;
import com.dugout.model.Team;
import com.dugout.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final CoachService coachService;

    public List<Team> getTeamsForCurrentCoach() {
        Coach coach = coachService.getCurrentAuthenticatedCoach();
        if (coach.getRole() == Role.ADMIN) {
            return teamRepository.findAll();
        }
        return teamRepository.findByCoachId(coach.getId());
    }

    public Team getTeamForCurrentCoach(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        Coach currentCoach = coachService.getCurrentAuthenticatedCoach();
        if (currentCoach.getRole() == Role.ADMIN) {
            return team;
        }

        if (!team.getCoach().getId().equals(currentCoach.getId())) {
            throw new AccessDeniedException("You do not have access to this team");
        }
        return team;
    }

    public Team createTeam(String name, String season) {
        Coach coach = coachService.getCurrentAuthenticatedCoach();
        Team team = Team.builder()
                .name(name)
                .season(season)
                .coach(coach)
                .build();
        return teamRepository.save(team);
    }

    public Team updateTeam(Long teamId, String name, String season) {
        Team team = getTeamForCurrentCoach(teamId);
        team.setName(name);
        team.setSeason(season);
        return teamRepository.save(team);
    }

    public void deleteTeam(Long teamId) {
        Team team = getTeamForCurrentCoach(teamId);
        teamRepository.delete(team);
    }
}
