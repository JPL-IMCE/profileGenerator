#!/bin/bash

set -ev

# Build if TRAVIS_TAG is unset or empty.
[ -n "${TRAVIS_TAG}" ] && exit 0;

# Build if there is no tag for the current commit.
t=$(git name-rev --tags --name-only $(git rev-parse HEAD))
[ -n "$t" ] && exit 0;

# No test
sbt -jvm-opts travis/jvmopts.compile compile
