# DND

## Build
From the project root:

| Action | Linux | Windows PowerShell |
| --- | --- | --- |
| Build and prepare the app | `./run build` | `.\run.bat build` |
| Run tests | `./run test` | `.\run.bat test` |
| Run the built app | `./run run` | `.\run.bat run` |
| Build and run | `./run` | `.\run.bat` |
| Show help | `./run --help` | `.\run.bat --help` |

Use JDK 25 for development. Gradle pins the compiler and test runtime to Java 25. The repository launchers apply Spotless formatting to Java sources before building, testing, or running. The `run` command uses the existing build. Build again after changing the source. `-h` also shows help.

On Linux, arguments after `./run run` are passed to the app. On Windows, pass application arguments directly to `app\build\install\app\bin\app.bat`.

## Pre-commit Java formatting

The first build, test, or launch through `./run` enables the repository’s pre-commit hook in that clone. The runner preserves a configured hooks path and any installed Git hooks. To enable the hook manually on a clone with no existing hooks, run `git config --local core.hooksPath .githooks`.

The hook runs Spotless on staged Java sources and stages any formatting fixes. If a file needs formatting and also contains unstaged edits, the hook stops the commit and leaves those edits in place. Commits without staged Java changes skip Spotless.

## CI and releases

GitHub Actions runs Java compiler linting, Spotless formatting checks, and JUnit tests on Ubuntu, Windows, and macOS for pushes to `main`, pull requests, and release tags. Windows and macOS also build the desktop distribution in CI. Run the checks locally with `./gradlew :app:check` and apply Java formatting with `./gradlew :app:spotlessApply`. CI uses JDK 25.

To release, set `version` in `app/build.gradle.kts` to a SemVer 2.0.0 version and commit it to `main`. Push a matching tag without a `v` prefix on a commit already reachable from remote `main`. CI rejects tags that do not match the Gradle version. For example, if the version is `1.2.3`:

```sh
git fetch origin main
git tag -a 1.2.3 origin/main -m "Release 1.2.3"
git push origin refs/tags/1.2.3
```

After verification passes on all three platforms, Actions publishes the Ubuntu-built desktop distribution as `DND.zip` on the GitHub release. A tag such as `1.2.3-rc.1` publishes a prerelease.
