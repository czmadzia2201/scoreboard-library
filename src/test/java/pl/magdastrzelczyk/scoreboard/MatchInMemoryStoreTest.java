package pl.magdastrzelczyk.scoreboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.magdastrzelczyk.scoreboard.exception.MatchNotFoundException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class MatchInMemoryStoreTest {

    private final MatchInMemoryStore matchInMemoryStore = new MatchInMemoryStore();

    private UUID matchId1;
    private UUID matchId2;
    private UUID matchId3;
    private UUID matchId4;

    @BeforeEach
    void setup() throws InterruptedException {
        matchId1 = initValuesInStore("Team A", "Team B", 1, 1);
        matchId2 = initValuesInStore("Team C", "Team D", 2, 3);
        matchId3 = initValuesInStore("Team E", "Team F", 2, 0);
        matchId4 = initValuesInStore("Team G", "Team H", 3, 1);
    }

    @Test
    void shouldThrowExceptionOnNonExistentMatchId() {
        UUID matchId = UUID.randomUUID();
        assertThatThrownBy(() -> matchInMemoryStore.get(matchId))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessage("Match not found: " + matchId);
    }

    @Test
    void shouldThrowExceptionOnNullMatchId() {
        assertThatThrownBy(() -> matchInMemoryStore.get(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Match id must not be null.");
    }

    @Test
    void shouldReturnSortedMatchesInProgress() {
        Match match4 = matchInMemoryStore.get(matchId4);
        match4.finish();
        List<Match> matches = matchInMemoryStore.getSortedMatchesInProgress();
        assertThat(matches).hasSize(3);
        assertThat(matches.stream().map(Match::getId).toList()).containsExactly(matchId2, matchId3, matchId1);
    }

    @Test
    void shouldCheckConflictedMatches_noConflict() {
        Match match5 = new Match("Team I", "Team J");
        assertThat(matchInMemoryStore.hasConflict(match5)).isFalse();
    }

    @Test
    void shouldCheckConflictedMatches_noConflictWithFinishedMatch() {
        Match match4 = matchInMemoryStore.get(matchId4);
        match4.finish();
        Match match5 = new Match("Team I", "Team G");
        assertThat(matchInMemoryStore.hasConflict(match5)).isFalse();
    }

    @Test
    void shouldCheckConflictedMatches_hasConflict() {
        Match match5 = new Match("Team I", "Team G");
        assertThat(matchInMemoryStore.hasConflict(match5)).isTrue();
    }

    private UUID initValuesInStore(String firstTeamId, String secondTeamId,
                                   int firstTeamScore, int secondTeamScore) throws InterruptedException {
        Match match = new Match(firstTeamId, secondTeamId);
        matchInMemoryStore.add(match);
        match.updateScore(firstTeamScore, secondTeamScore);
        Thread.sleep(10);
        return match.getId();
    }

}