#!/bin/sh
set -eu

branch=${1:-$(git symbolic-ref --quiet --short HEAD || true)}
if [ "$branch" = main ] && [ "${2:-}" = --allow-main ]; then
  exit 0
fi

if ! printf '%s\n' "$branch" | LC_ALL=C grep -Eq '^(feat|fix|chore|docs)/[a-z0-9]+(-[a-z0-9]+)*$'; then
  printf '%s\n' "Invalid branch: $branch" >&2
  printf '%s\n' 'Use feat/, fix/, chore/, or docs/ followed by a lowercase kebab-case description.' >&2
  printf '%s\n' 'Example: git checkout -b chore/repository-workflow' >&2
  exit 1
fi

git check-ref-format "refs/heads/$branch"
