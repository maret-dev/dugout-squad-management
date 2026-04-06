package com.dugout.service;

import com.dugout.model.Player;
import com.dugout.model.Team;
import com.dugout.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
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
        return playerRepository.save(player);
    }

    public void removePlayer(Long teamId, Long playerId) {
        teamService.getTeamForCurrentCoach(teamId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        playerRepository.delete(player);
    }
}
