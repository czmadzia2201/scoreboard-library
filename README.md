# Scoreboard Library

A simple Java library for managing a live football scoreboard.

The following sections describe the assumptions, design decisions and trade-offs made during the implementation. 
Since these topics are closely related, they are presented together by subject rather than separated into independent sections.

My objective was to keep the library clean and simple rather than complicate it with features beyond the requirements. 
Several possible extensions were deliberately not added, 
such as team management, additional match states (for example `not started`), additional scoring options, etc.

### Live scoreboard

A match starts immediately when `startMatch(...)` is called. There is no separate `scheduled` or `not started` state.
Finished matches cannot be updated or reopened. A final score should be recorded before calling `finishMatch(...)`.

### Team identifiers

I assumed the library should be team-agnostic and only require team identifiers to manage matches.
Therefore, it does not define a `Team` class. The public API accepts team identifiers as `String` values.
This approach allows the library to be flexible and work with different team identification schemes.
In the examples, country names are used as team identifiers for readability. A real application could map 
these identifiers to display names, flags, countries, or other team metadata outside of this library.

### Match identifiers

Each match is identified by a `UUID` generated when the match is created.
UUIDs were chosen because the library does not rely on a database sequence or external id generator.
This keeps match creation simple while still providing unique identifiers for later updates, finishing, and summary retrieval.

### Score rules

Scores cannot be negative and cannot decrease.
The update operation accepts the current score rather than a score delta (+/-n). Both team scores must be provided.
I assumed this library provides a backend API, not a complete user interface. 
Applications built on top of it may offer convenience operations, such as increasing the score of a single team.

### Storage

Matches are stored in memory inside the scoreboard instance. The library does not provide persistence. 

### Match state

I considered introducing a `MatchStatus` enum.
I decided not to add it because the current model only distinguishes between matches in progress and finished matches. 
This state can be derived from `endTime`, and the domain method `isFinished()` keeps the code readable without storing redundant state.

### Match validation

Team identifiers are validated inside the `Match` constructor.
A Match object is created before checking for conflicts so that all validation rules remain in one place.
This is a small cost that allows clear responsibility separation (validation in constructor, 
conflict check in the in-memory store). 

### Thread safety

The public scoreboard operations are synchronized.
This ensures that operations such as "check for conflict and start match" are atomic. 
A `ConcurrentHashMap` alone would not be sufficient for this, because it would make individual map operations thread-safe but not multi-step scoreboard operations.
More advanced locking strategies, such as `ReadWriteLock`, were intentionally avoided to keep the implementation simple and appropriate for the size of the task.

## Usage / Public API

```java
Scoreboard scoreboard = new InMemoryScoreboard();

UUID matchId = scoreboard.startMatch("Mexico", "Canada");

scoreboard.updateScore(matchId, 0, 5);

List<Match> summary = scoreboard.getSummary();

scoreboard.finishMatch(matchId);

