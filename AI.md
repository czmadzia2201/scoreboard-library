# AI Usage

This project was developed with assistance from AI tools.
AI was used primarily as a discussion partner, reviewer, and productivity aid rather than as a code generator.

## GitHub Copilot (IntelliJ)

GitHub Copilot was used for:

- code completion
- generating two methods based on explicit prompts (listed below)

### Prompt 1

> In class MatchInMemoryStore implement method hasConflict (stub in place). Rules:
> - filter out finished matches
> - compare each teamId of the input match with both teamIds of existing matches
> - return true if anyMatch

### Prompt 2

> In class Match implement method compareTo. Rules:
> - higher combined score goes first
> - in case of tie: more recently started match goes first

(Finally, I decided to use a custom Comparator instead of implementing Comparable.)

## ChatGPT

ChatGPT was used extensively throughout the development process for:

- discussing API design, architectural decisions and trade-offs
- code reviews
- identifying edge cases
- reviewing naming and code readability
- improving the wording of the project documentation (README and this document)

Most discussions focused on design decisions rather than implementation details.
Typical discussion topics included API design, encapsulation, thread safety, naming, and validation rules.

The implementation was done manually, with the exceptions mentioned above.
AI-generated suggestions were treated as proposals rather than final solutions.