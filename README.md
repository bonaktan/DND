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

Use JDK 25 for development. Gradle pins the compiler and test runtime to Java 25. The `run` command uses the existing build and needs a Java 25 runtime. Build again after changing the source. `-h` also shows help.

On Linux, arguments after `./run run` are passed to the app. On Windows, pass application arguments directly to `app\build\install\app\bin\app.bat`.

## CI and releases

GitHub Actions runs Java compiler linting, Spotless formatting checks, and JUnit tests on pushes to `main` and on pull requests. Run the same checks locally with `./gradlew :app:check` and apply Java formatting with `./gradlew :app:spotlessApply`. CI uses JDK 25.

To release, push a new SemVer 2.0.0 tag such as `1.2.3` on a commit already reachable from remote `main`. Use the version as the tag name without a `v` prefix.

```sh
git fetch origin main
git tag -a 1.2.3 origin/main -m "Release 1.2.3"
git push origin refs/tags/1.2.3
```

After verification passes, Actions publishes the desktop distribution as `DND.zip` on the GitHub release. A tag such as `1.2.3-rc.1` publishes a prerelease.
