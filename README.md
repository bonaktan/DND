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

`-h` also shows help. The `run` command uses the existing build; build again after changing the source. Running needs a compatible Java runtime; building and testing require a JDK.

On Linux, arguments after `./run run` are passed to the app. On Windows, pass application arguments directly to `app\build\install\app\bin\app.bat`.
