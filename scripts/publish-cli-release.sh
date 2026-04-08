#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-}"

if [[ -z "$VERSION" ]]; then
  echo "Usage: $0 <version>"
  echo "Example: $0 0.1.0"
  exit 1
fi

TAG="cli-v$VERSION"

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean. Commit or stash your changes first."
  exit 1
fi

if git rev-parse "$TAG" >/dev/null 2>&1; then
  echo "Tag $TAG already exists locally."
  exit 1
fi

if git ls-remote --tags origin | grep -q "refs/tags/$TAG$"; then
  echo "Tag $TAG already exists on origin."
  exit 1
fi

git tag -a "$TAG" -m "CLI release $TAG"
git push origin "$TAG"

echo "Tag $TAG pushed."
echo "GitHub Actions will now build and publish the CLI release."