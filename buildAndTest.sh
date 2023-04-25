#!bin/bash
# activate bash checks for unset vars, pipe fails
set -eauo pipefail
SCRIPT=$(readlink -f "$0")
SCRIPT_DIR=$(dirname "$SCRIPT")

./gradlew \
    --info \
    --console=plain \
    clean \
    export \
    testrun.1-integration-pop

echo '### create Coverage and PoP reports'
. ${SCRIPT_DIR}/eim.pop/jacoco/createCoverageReports.sh
