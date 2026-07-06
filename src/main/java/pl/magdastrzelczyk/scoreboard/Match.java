package pl.magdastrzelczyk.scoreboard;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Match {
    private final UUID id;
    private final String firstTeamId;
    private final String secondTeamId;
    private int firstTeamScore;
    private int secondTeamScore;
    private final Instant startTime;
    private Instant endTime;

    Match(String firstTeamId, String secondTeamId) {
        validateMatch(firstTeamId, secondTeamId);
        this.id = UUID.randomUUID();
        this.firstTeamId = firstTeamId;
        this.secondTeamId = secondTeamId;
        this.firstTeamScore = 0;
        this.secondTeamScore = 0;
        this.startTime = Instant.now();
    }

    void finish() {
        if (isFinished()) {
            throw new IllegalStateException("Match is already finished.");
        }
        this.endTime = Instant.now();
    }

    void updateScore(int firstTeamScore, int secondTeamScore) {
        if (isFinished()) {
            throw new IllegalStateException("Finished match cannot be updated.");
        }
        if (firstTeamScore < 0 || secondTeamScore < 0) {
            throw new IllegalArgumentException("Scores must not be negative.");
        }
        if (firstTeamScore < this.firstTeamScore || secondTeamScore < this.secondTeamScore) {
            throw new IllegalArgumentException("Scores must not be lower than the current scores.");
        }
        this.firstTeamScore = firstTeamScore;
        this.secondTeamScore = secondTeamScore;
    }

    int getTotalScore() {
        return firstTeamScore + secondTeamScore;
    }

    boolean involves(String teamId) {
        return firstTeamId.equals(teamId) || secondTeamId.equals(teamId);
    }

    boolean isFinished() {
        return endTime != null;
    }

    @Override
    public String toString() {
        return String.format("%s %d - %s %d", firstTeamId, firstTeamScore, secondTeamId, secondTeamScore);
    }

    private void validateMatch(String firstTeamId, String secondTeamId) {
        validateTeamId(firstTeamId);
        validateTeamId(secondTeamId);
        if (firstTeamId.equals(secondTeamId)) {
            throw new IllegalArgumentException("First team and second team must not be the same.");
        }
    }

    private void validateTeamId(String teamId) {
        if (teamId == null || teamId.isBlank()) {
            throw new IllegalArgumentException("Team id must not be null or blank.");
        }
    }

}

