#!bin/bash
# activate bash checks for unset vars, pipe fails
set -eauo pipefail
SCRIPT=$(readlink -f "$0")
SCRIPT_DIR=$(dirname "$SCRIPT")

/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/klibio/bootstrap/main/install-klibio.sh)" bash -j

./gradlew \
    --info \
    --console=plain \
    clean \
    export \
    testrun.1-integration-pop

echo '### create Coverage and PoP reports'
. ${SCRIPT_DIR}/eim.pop/jacoco/createCoverageReports.sh
