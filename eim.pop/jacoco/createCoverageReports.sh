script_dir=$(dirname $(readlink -f $0))

if [ "${CI}" = "true" ]; then
  workspace_dir=$(cd "${script_dir}"; pwd)
else
  workspace_dir=$(cd "${script_dir}/../.."; pwd)
fi
jacoco_dir=$(cd "${workspace_dir}/eim.pop/jacoco"; pwd)

source_files=${jacoco_dir}/rt/source_files
class_files=${jacoco_dir}/rt/class_files
mkdir -p ${source_files}
mkdir -p ${class_files}

find . -iname "src" \
  ! -path "*/generated/*" \
  ! -path "*/jacoco/*" \
  -type d \
  -exec cp -R {} ${source_files} \;

find . -iname "classes" \
  ! -path "*/jacoco/*" \
  -type d \
  -exec cp -R {} ${class_files} \;

mkdir -p ${script_dir}/reports
java -jar  ${script_dir}/jacococli.jar \
    report ${jacoco_dir}/exec/1-integration-pop_jacoco.exec \
    --sourcefiles ${source_files}/src \
    --classfiles ${class_files} \
    --encoding UTF-8 \
    --html ${jacoco_dir}/reports \
    --name "POP coverage for the Eclipse Installation Manager" \
    --xml ${jacoco_dir}/reports/1-integration-pop_jacoco.xml