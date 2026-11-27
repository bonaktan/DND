# AGENTS.md

## Repository basics

- DND is a Java 25 desktop game. Java sources live in `app/src/` and tests in `app/test/`.
- From the repo root, use `./run build` to build and `./run test` to test. See [README.md](README.md) for launch and formatting commands.
- Game content and media live in `assets/`. See [assets/README.md](assets/README.md) for formats and validation.

## Mandatory media rule

Never generate media under any circumstances, including temporary assets and placeholders. If the task requires media that is missing from the repository, immediately interrupt the turn, ask the user to provide the specific missing media, and wait for it before resuming.
