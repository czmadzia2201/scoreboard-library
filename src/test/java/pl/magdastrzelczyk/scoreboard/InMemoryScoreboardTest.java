package pl.magdastrzelczyk.scoreboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.magdastrzelczyk.scoreboard.exception.ConflictingMatchException;
import pl.magdastrzelczyk.scoreboard.exception.MatchNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class InMemoryScoreboardTest {

    private final MatchInMemoryStore matchInMemoryStore = new MatchInMemoryStore();

    private final Scoreboard scoreboard = new InMemoryScoreboard(matchInMemoryStore);

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
    void shouldStartNewMatch_happyPath() {
        UUID matchId = scoreboard.startMatch("Team I", "Team J");
        assertThat(matchInMemoryStore.getSortedMatchesInProgress()).hasSize(5);
        Match match = matchInMemoryStore.get(matchId);
        assertThat(match).isNotNull();
        assertThat(matchId).isEqualTo(match.getId());
        assertThat(match.getFirstTeamId()).isEqualTo("Team I");
        assertThat(match.getSecondTeamId()).isEqualTo("Team J");
        assertThat(match.getFirstTeamScore()).isEqualTo(0);
        assertThat(match.getSecondTeamScore()).isEqualTo(0);
        assertThat(match.getStartTime()).isNotNull();
        assertThat(match.getStartTime()).isBetween(Instant.now().minusSeconds(10), Instant.now());
        assertThat(match.getEndTime()).isNull();
    }

    @Test
    void shouldNotStartNewMatch_nullTeamId() {
        assertThatThrownBy(() -> scoreboard.startMatch("Team I", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team id must not be null or blank.");
        assertThat(matchInMemoryStore.getSortedMatchesInProgress()).hasSize(4);
    }

    @Test
    void shouldNotStartNewMatch_conflictingMatch() {
        assertThatThrownBy(() -> scoreboard.startMatch("Team I", "Team G"))
                .isInstanceOf(ConflictingMatchException.class)
                .hasMessageContaining("At least one of the teams is currently playing another match.");
        assertThat(matchInMemoryStore.getSortedMatchesInProgress()).hasSize(4);
    }

    @Test
    void shouldUpdateScore() {
        scoreboard.updateScore(matchId1, 2, 2);
        Match match1 = matchInMemoryStore.get(matchId1);
        assertThat(match1.getFirstTeamScore()).isEqualTo(2);
        assertThat(match1.getSecondTeamScore()).isEqualTo(2);
    }

    @Test
    void shouldNotUpdateScore_nullMatchId() {
        assertThatThrownBy(() -> scoreboard.updateScore(null, 2, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Match id must not be null.");
    }

    @Test
    void shouldNotUpdateScore_notFoundMatchId() {
        UUID matchId = UUID.randomUUID();
        assertThatThrownBy(() -> scoreboard.updateScore(matchId, 2, 2))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessageContaining("Match not found: " + matchId);
    }

    @Test
    void shouldFinishMatch() {
        scoreboard.finishMatch(matchId1);
        Match match1 = matchInMemoryStore.get(matchId1);
        assertThat(match1.isFinished()).isTrue();
        assertThat(match1.getEndTime()).isBetween(Instant.now().minusSeconds(10), Instant.now());
    }

    @Test
    void shouldNotFinishMatch_nullMatchId() {
        assertThatThrownBy(() -> scoreboard.finishMatch(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Match id must not be null.");
    }

    @Test
    void shouldNotFinishMatch_notFoundMatchId() {
        UUID matchId = UUID.randomUUID();
        assertThatThrownBy(() -> scoreboard.finishMatch(matchId))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessageContaining("Match not found: " + matchId);
    }

    @Test
    void shouldAllowReusingTeamAfterMatchFinished() {
        scoreboard.finishMatch(matchId1);
        UUID matchId = scoreboard.startMatch("Team A", "Team J");
        assertThat(matchInMemoryStore.getSortedMatchesInProgress()).hasSize(4);
        Match match = matchInMemoryStore.get(matchId);
        assertThat(match).isNotNull();
        assertThat(matchId).isEqualTo(match.getId());
        assertThat(match.getFirstTeamId()).isEqualTo("Team A");
        assertThat(match.getSecondTeamId()).isEqualTo("Team J");
    }

    @Test
    void shouldGetSummaryOfMatchesInProgress() {
        List<Match> matches = scoreboard.getSummary();
        assertThat(matches).hasSize(4);
        assertThat(matches.stream().map(Match::getId).toList()).containsExactly(matchId2, matchId4, matchId3, matchId1);
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