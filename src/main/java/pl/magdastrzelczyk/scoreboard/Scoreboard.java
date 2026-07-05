package pl.magdastrzelczyk.scoreboard;

import java.util.List;
import java.util.UUID;

public interface Scoreboard {
    UUID startMatch(String firstTeamId, String secondTeamId);

    void updateScore(UUID matchId, int firstTeamScore, int secondTeamScore);

    void finishMatch(UUID matchId);

    List<Match> getSummary();
}
