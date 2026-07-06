package pl.magdastrzelczyk.scoreboard;

import org.junit.jupiter.api.Test;
import pl.magdastrzelczyk.scoreboard.exception.ConflictingMatchException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

class UseCaseScenarioTest {

    private final Scoreboard scoreboard = new InMemoryScoreboard();

    @Test
    void shouldHandleCompleteScoreboardFlow() {
        UUID matchId1 = scoreboard.startMatch("Mexico", "Canada");
        UUID matchId2 = scoreboard.startMatch("USA", "Brazil");

        scoreboard.updateScore(matchId1, 2, 1);
        scoreboard.updateScore(matchId2, 2, 2);

        assertThatThrownBy(() -> scoreboard.updateScore(matchId1, 3, 0))
                        .isInstanceOf(IllegalArgumentException.class);

        scoreboard.updateScore(matchId1, 3, 3);

        assertThatThrownBy(() -> scoreboard.startMatch("Mexico", "Brazil"))
                        .isInstanceOf(ConflictingMatchException.class);

        UUID matchId3 = scoreboard.startMatch("Italy", "Germany");
        UUID matchId4 = scoreboard.startMatch("Poland", "Chile");

        scoreboard.updateScore(matchId3, 1, 3);
        scoreboard.updateScore(matchId1, 4, 4);

        List<Match> summary1 = scoreboard.getSummary();
        assertThat(summary1)
                .hasSize(4)
                .extracting(Match::getId)
                .containsExactly(matchId1, matchId3, matchId2, matchId4);
        assertThat(summary1)
                .extracting(Match::getFirstTeamScore, Match::getSecondTeamScore)
                .containsExactly(tuple(4, 4), tuple(1, 3), tuple(2, 2), tuple(0, 0));

        scoreboard.finishMatch(matchId1);
        scoreboard.updateScore(matchId4, 0, 5);

        List<Match> summary2 = scoreboard.getSummary();
        assertThat(summary2)
                .hasSize(3)
                .extracting(Match::getId)
                .containsExactly(matchId4, matchId3, matchId2);
        assertThat(summary2)
                .extracting(Match::getFirstTeamScore, Match::getSecondTeamScore)
                .containsExactly(tuple(0, 5), tuple(1, 3), tuple(2, 2));

        assertThatThrownBy(() -> scoreboard.updateScore(matchId1, 4, 3))
                .isInstanceOf(IllegalStateException.class);

        UUID matchId5 = scoreboard.startMatch("Mexico", "France");

        scoreboard.updateScore(matchId5, 1, 1);

        List<Match> summary3 = scoreboard.getSummary();
        assertThat(summary3)
                .hasSize(4)
                .extracting(Match::getId)
                .containsExactly(matchId4, matchId3, matchId2, matchId5);
        assertThat(summary3)
                .extracting(Match::getFirstTeamScore, Match::getSecondTeamScore)
                .containsExactly(tuple(0, 5), tuple(1, 3), tuple(2, 2), tuple(1, 1));
    }

}
