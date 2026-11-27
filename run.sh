#!/bin/sh
app_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$app_dir" || exit 1

setup_pre_commit() {
    [ -e .git ] && [ -x .githooks/pre-commit ] && command -v git >/dev/null 2>&1 || return 0
    if git config --get core.hooksPath >/dev/null 2>&1; then
        return 0
    fi

    hooks_dir=$(git rev-parse --git-path hooks) || return 0
    pre_commit="$hooks_dir/pre-commit"
    if [ -e "$pre_commit" ] || [ -L "$pre_commit" ]; then
        return 0
    fi

    for hook in "$hooks_dir"/*; do
        case "$hook" in *.sample) continue ;; esac
        if [ -f "$hook" ] && [ -x "$hook" ]; then
            # Keep the default hooks directory active when it already has other hooks.
            (set -C; printf '%s\n' '#!/bin/sh' 'exec ./.githooks/pre-commit "$@"' > "$pre_commit") &&
                chmod +x "$pre_commit" || echo "Could not enable the pre-commit hook." >&2
            return 0
        fi
    done

    git config --local core.hooksPath .githooks ||
        echo "Could not enable the pre-commit hook." >&2
}

case "${1-}" in
    ''|build|test|run)
        setup_pre_commit
        ./gradlew :app:spotlessApply || exit $?
        ;;
esac

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
