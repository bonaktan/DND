#!/bin/sh
app_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$app_dir" || exit 1

if [ "$#" -eq 0 ]; then
    ./gradlew build :app:installDist || exit $?
    set -- run
fi

case "$1" in
    build) exec ./gradlew build :app:installDist ;;
    test) exec ./gradlew test ;;
    -h|--help)
        echo "Usage: ./run [build|test|run|-h|--help]"
        echo "  No command builds and runs the app."
        echo "  build: build and prepare the app; test: run tests; run: start the built app."
        exit 0
        ;;
    run)
        [ "$#" -eq 0 ] || shift
        launcher="$app_dir/app/build/install/app/bin/app"
        if [ ! -f "$launcher" ]; then
            echo "Application not built yet. Run ./run build first." >&2
            exit 1
        fi
        exec "$launcher" "$@"
        ;;
    *) echo "Usage: ./run [build|test|run|-h|--help]" >&2; exit 1 ;;
esac
