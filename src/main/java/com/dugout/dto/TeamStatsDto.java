package com.dugout.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TeamStatsDto {
    private List<PlayerStatsDto> playerStats;
    private List<PlayerStatsDto> top3Training;
    private List<PlayerStatsDto> top3Match;
}
