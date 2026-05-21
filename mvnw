#!/usr/bin/env sh
# ----------------------------------------------------------------------------
# Maven wrapper
# ----------------------------------------------------------------------------
# This file is part of the Maven Wrapper.
# For more information, see https://github.com/takari/maven-wrapper

SCRIPT_DIR="$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)"
java -jar "$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.jar" "$@"
