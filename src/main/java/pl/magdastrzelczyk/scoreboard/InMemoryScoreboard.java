package pl.magdastrzelczyk.scoreboard;

import pl.magdastrzelczyk.scoreboard.exception.ConflictingMatchException;

import java.util.List;
import java.util.UUID;

public class InMemoryScoreboard implements Scoreboard {

    private final MatchInMemoryStore matchStore;

    public InMemoryScoreboard() {
        this(new MatchInMemoryStore());
    }

    InMemoryScoreboard(MatchInMemoryStore matchStore) {
        this.matchStore = matchStore;
    }

    @Override
    public synchronized UUID startMatch(String firstTeamId, String secondTeamId) {
        Match match = new Match(firstTeamId, secondTeamId);
        if (matchStore.hasConflict(match)) {
            throw new ConflictingMatchException();
        }
        matchStore.add(match);
        return match.getId();
    }

    @Override
    public synchronized void updateScore(UUID matchId, int firstTeamScore, int secondTeamScore) {
        Match match = matchStore.get(matchId);
        match.updateScore(firstTeamScore, secondTeamScore);
    }

    @Override
    public synchronized void finishMatch(UUID matchId) {
        Match match = matchStore.get(matchId);
        match.finish();
    }

    @Override
    public synchronized List<Match> getSummary() {
        return matchStore.getSortedMatchesInProgress();
    }
}
