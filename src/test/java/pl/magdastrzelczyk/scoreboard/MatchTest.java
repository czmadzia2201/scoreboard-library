package pl.magdastrzelczyk.scoreboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class MatchTest {

    private Match match;

    @BeforeEach
    void setUp() {
        match = new Match("Team A", "Team B");
    }

    @Test
    void shouldNotCreateMatchWithEmptyTeamId() {
        assertThatThrownBy(() -> new Match("", "Team B"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Team id must not be null or blank.");
    }

    @Test
    void shouldNotCreateMatchWithSameTeamId() {
        assertThatThrownBy(() -> new Match("Team A", "Team A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First team and second team must not be the same.");
    }

    @Test
    void shouldUpdateMatchScore() {
        match.updateScore(2, 1);
        assertThat(match.getFirstTeamScore()).isEqualTo(2);
        assertThat(match.getSecondTeamScore()).isEqualTo(1);
    }

    @Test
    void shouldCreateScoreHistory() throws InterruptedException {
        Instant before = Instant.now();

        match.updateScore(2, 1);
        Thread.sleep(10);
        match.updateScore(3, 1);
        Thread.sleep(10);
        match.updateScore(3, 3);

        Instant after = Instant.now();

        List<MatchScoreSnapshot> scoreHistory = match.getScoreHistory();
        assertThat(scoreHistory)
                .hasSize(3)
                .extracting(MatchScoreSnapshot::firstTeamScore, MatchScoreSnapshot::secondTeamScore)
                .containsExactly(tuple(2, 1), tuple(3, 1), tuple(3, 3));

        assertThat(scoreHistory)
                .extracting(MatchScoreSnapshot::updateTime)
                .isSorted()
                .allSatisfy(instant -> assertThat(instant).isBetween(before, after));
    }

    @Test
    void shouldNotDecreaseTeamScore() {
        match.updateScore(5, 4);
        assertThatThrownBy(() -> match.updateScore(2, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Scores must not be lower than the current scores.");
    }

    @Test
    void shouldNotUpdateMatchWithNegativeNumber() {
        assertThatThrownBy(() -> match.updateScore(-1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Scores must not be negative.");
    }

    @Test
    void shouldNotUpdateFinishedMatch() {
        match.finish();
        assertThatThrownBy(() -> match.updateScore(2, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Finished match cannot be updated.");
    }

    @Test
    void shouldNotFinishMatchMoreThanOnce() {
        match.finish();
        assertThat(match.getEndTime()).isNotNull();
        assertThatThrownBy(() -> match.finish())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Match is already finished.");
    }

}