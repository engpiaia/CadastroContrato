@echo off
setlocal
set MAVEN_PROJECTBASEDIR=%~dp0
if not exist "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar" (
  echo ERROR: Maven Wrapper JAR not found in %MAVEN_PROJECTBASEDIR%\.mvn\wrapper\
  exit /b 1
)
java -jar "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar" %*
