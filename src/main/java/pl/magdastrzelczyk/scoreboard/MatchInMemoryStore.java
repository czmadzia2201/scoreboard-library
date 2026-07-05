package pl.magdastrzelczyk.scoreboard;

import pl.magdastrzelczyk.scoreboard.exception.MatchNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class MatchInMemoryStore {

    private static final Comparator<Match> SUMMARY_ORDER =
            Comparator.comparingInt(Match::getTotalScore).reversed()
                    .thenComparing(Match::getStartTime, Comparator.reverseOrder());

    private final Map<UUID, Match> matches = new ConcurrentHashMap<>();

    void add(Match match) {
        matches.put(match.getId(), match);
    }

    public Match get(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Match id must not be null.");
        }
        Match match = matches.get(id);
        if (match == null) {
            throw new MatchNotFoundException(id);
        }
        return match;
    }

    public List<Match> getSortedMatchesInProgress() {
        return matches.values().stream()
                .filter(m -> !m.isFinished())
                .sorted(SUMMARY_ORDER)
                .toList();
    }

    public boolean hasConflict(Match match) {
        return matches.values().stream()
                .filter(m -> !m.isFinished())
                .anyMatch(m ->
                        m.involves(match.getFirstTeamId()) ||
                        m.involves(match.getSecondTeamId())
                );
    }

}