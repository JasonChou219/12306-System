#!/usr/bin/env bash
set -euo pipefail

TYPE="${1:-}"
NAME="${2:-}"

if [[ -z "$TYPE" || -z "$NAME" ]]; then
  echo "Usage: ./create-module.sh <lib|app> <module-name>"
  exit 1
fi

TEMPLATE_DIR="module-template-${TYPE}"
if [[ ! -d "$TEMPLATE_DIR" ]]; then
  echo "Template not found: $TEMPLATE_DIR"
  exit 1
fi

if [[ -d "$NAME" ]]; then
  echo "Module already exists: $NAME"
  exit 1
fi

# 1) copy template
cp -R "$TEMPLATE_DIR" "$NAME"

# 2) replace placeholders in pom.xml and yml/java
# 你的基础包名：按你项目实际改
BASE_PACKAGE="com.example.train.${NAME}"
BASE_PACKAGE_PATH="$(echo "$BASE_PACKAGE" | tr '.' '/')"

# 替换 __MODULE__
perl -pi -e "s/__MODULE__/${NAME}/g" "$NAME/pom.xml" || true

# 替换 package 占位（java）
# 把 __PKG__ 目录替换成实际 package path
mkdir -p "$NAME/src/main/java/$BASE_PACKAGE_PATH"
mkdir -p "$NAME/src/test/java/$BASE_PACKAGE_PATH"
rm -rf "$NAME/src/main/java/__PKG__" "$NAME/src/test/java/__PKG__" 2>/dev/null || true

# 如果是 app 模板，还要替换启动类
if [[ "$TYPE" == "app" ]]; then
  MAIN_CLASS="$(tr '[:lower:]-' '[:upper:]_' <<< "${NAME}")"
  MAIN_CLASS="$(echo "${NAME}" | awk -F- '{for(i=1;i<=NF;i++){ $i=toupper(substr($i,1,1)) substr($i,2)} }1' OFS='')Application"

  # 生成启动类文件
  cat > "$NAME/src/main/java/$BASE_PACKAGE_PATH/${MAIN_CLASS}.java" <<EOF
package ${BASE_PACKAGE};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ${MAIN_CLASS} {
    public static void main(String[] args) {
        SpringApplication.run(${MAIN_CLASS}.class, args);
    }
}
EOF

  # application.yml 替换模块名
  if [[ -f "$NAME/src/main/resources/application.yml" ]]; then
    perl -pi -e "s/__MODULE__/${NAME}/g" "$NAME/src/main/resources/application.yml" || true
  fi
fi

# 3) register module in parent pom.xml (insert before </modules>)
if ! grep -q "<module>${NAME}</module>" pom.xml; then
  perl -0777 -pi -e "s#</modules>#  <module>${NAME}</module>\n</modules>#s" pom.xml
fi

echo "Created module: $NAME (type=$TYPE)"
echo "Now run: ./mvnw -q clean compile"
