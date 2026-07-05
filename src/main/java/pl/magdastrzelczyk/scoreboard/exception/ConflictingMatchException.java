package pl.magdastrzelczyk.scoreboard.exception;

public class ConflictingMatchException extends RuntimeException {

    public ConflictingMatchException() {
        super("At least one of the teams is currently playing another match.");
    }
}
