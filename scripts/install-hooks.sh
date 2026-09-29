#!/bin/sh
set -eu

cd "$(git rev-parse --show-toplevel)"
current=$(git config --get core.hooksPath || true)
if [ -n "$current" ] && [ "$current" != .githooks ]; then
  printf '%s\n' "Existing hooks path '$current' must be reconciled before installing .githooks." >&2
  exit 1
fi
git config --local core.hooksPath .githooks
printf '%s\n' 'Installed repository pre-commit hook (branch name, staged formatting, and lint).'
