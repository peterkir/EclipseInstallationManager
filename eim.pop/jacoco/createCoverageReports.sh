set -Eeuo pipefail

SCRIPT_DIR="$( cd $( dirname ${BASH_SOURCE[0]} ) >/dev/null 2>&1 && pwd )" &&
  echo $SCRIPT_DIR && \
  pushd $SCRIPT_DIR/../..

SOURCES=$SCRIPT_DIR/sources
CLASSES=$SCRIPT_DIR/classes
mkdir -p $SOURCES
mkdir -p $CLASSES

cp -R eim/src \
      $SOURCES

cp -R eim/bin \
      $CLASSES

popd

mkdir -p $SCRIPT_DIR/reports
java -jar  $SCRIPT_DIR/jacococli.jar \
    report $SCRIPT_DIR/../generated/tmp/testrun.1-integration-pop/1-integration-pop_jacoco.exec \
    --sourcefiles ${SOURCES}/src \
    --classfiles ${CLASSES} \
    --encoding UTF-8 \
    --html $SCRIPT_DIR/reports \
    --name "POP coverage for the Eclipse Installation Manager" \
    --xml $SCRIPT_DIR/reports/1-integration-pop_jacoco.xml
