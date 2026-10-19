#!/bin/sh
# Gradle Wrapper launcher placeholder. The standard wrapper JAR must be generated
# or restored before this script can bootstrap Gradle in a clean environment.
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
if [ ! -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
  echo "Gradle wrapper JAR is missing. Run 'gradle wrapper --gradle-version 8.13' in an environment with Gradle installed, or use Android Studio's Gradle wrapper generation." >&2
  exit 1
fi
exec java -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
