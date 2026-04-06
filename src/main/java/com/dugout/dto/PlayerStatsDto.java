package com.dugout.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerStatsDto {
    private Long playerId;
    private String playerName;
    private int trainingPresent;
    private int trainingTotal;
    private double trainingPercentage;
    private int matchPresent;
    private int matchTotal;
    private double matchPercentage;
    private boolean lowAttendanceFlag;
}
