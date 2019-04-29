#!/bin/bash
#
# Usage:
#
#   ./release.sh :major
#   ./release.sh :minor
#   ./release.sh :patch
#

lein release $1 && lein release-deploy && lein release-push && echo "Project was successfully released."
