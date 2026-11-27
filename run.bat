@echo off
setlocal
pushd "%~dp0" || exit /b 1
if "%~1"=="" goto format
if /i "%~1"=="run" goto format
if /i "%~1"=="build" goto format
if /i "%~1"=="test" goto format
if /i "%~1"=="-h" goto help
if /i "%~1"=="--help" goto help
echo Usage: run.bat [build^|test^|run^|-h^|--help] 1>&2
exit /b 1

:help
echo Usage: run.bat [build^|test^|run^|-h^|--help]
echo   No command builds and runs the app.
echo   build: build and prepare the app; test: run tests; run: start the built app.
exit /b 0

:format
call gradlew.bat :app:spotlessApply
if errorlevel 1 exit /b %errorlevel%
if /i "%~1"=="run" goto run
if /i "%~1"=="build" goto build
if /i "%~1"=="test" goto test

:buildrun
call gradlew.bat build :app:installDist
if errorlevel 1 exit /b %errorlevel%
goto run

:build
call gradlew.bat build :app:installDist
exit /b %errorlevel%

:test
call gradlew.bat test
exit /b %errorlevel%

:run
if not exist "app\build\install\app\bin\app.bat" (
    echo Application not built yet. Run run.bat build first. 1>&2
    exit /b 1
)
if not "%~2"=="" (
    echo Passing application arguments is not supported by run.bat. 1>&2
    exit /b 1
)
call "app\build\install\app\bin\app.bat"
exit /b %errorlevel%
