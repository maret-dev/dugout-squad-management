package com.dugout.repository;

import com.dugout.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamIdAndActiveTrue(Long teamId);
    List<Player> findByTeamId(Long teamId);
}
