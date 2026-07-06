package pl.magdastrzelczyk.scoreboard;

import java.time.Instant;

public record MatchScoreSnapshot(
        int firstTeamScore,
        int secondTeamScore,
        Instant updateTime
) {
}
