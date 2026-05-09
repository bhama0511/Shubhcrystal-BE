@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------
@IF "%MAVEN_BATCH_ECHO%"=="on" echo %MAVEN_BATCH_ECHO%
@SETLOCAL enableextensions
@IF NOT "%MAVEN_BATCH_ECHO%"=="on" echo off

IF "%HOME%"=="" (SET "HOME=%HOMEDRIVE%%HOMEPATH%")

SET "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" GOTO endDetectBaseDir

SET "EXEC_DIR=%CD%"
SET "WDIR=%EXEC_DIR%"
:findBaseDir
IF EXIST "%WDIR%"\.mvn GOTO baseDirFound
cd ..
IF "%WDIR%"=="%CD%" GOTO baseDirNotFound
SET "WDIR=%CD%"
GOTO findBaseDir

:baseDirFound
SET "MAVEN_PROJECTBASEDIR=%WDIR%"
cd "%EXEC_DIR%"
GOTO endDetectBaseDir

:baseDirNotFound
SET "MAVEN_PROJECTBASEDIR=%EXEC_DIR%"
cd "%EXEC_DIR%"

:endDetectBaseDir

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config" GOTO endReadAdditionalConfig
@setlocal EnableExtensions EnableDelayedExpansion
FOR /F "usebackq delims=" %%a IN ("%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config") DO SET "JVM_CONFIG_MAVEN_PROPS=!JVM_CONFIG_MAVEN_PROPS! %%a"
@endlocal & SET JVM_CONFIG_MAVEN_PROPS=%JVM_CONFIG_MAVEN_PROPS%
:endReadAdditionalConfig

SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
SET "DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="wrapperUrl" SET "DOWNLOAD_URL=%%B"
)

IF EXIST %WRAPPER_JAR% GOTO skipDownload

IF "%MVNW_VERBOSE%"=="true" ECHO Downloading %WRAPPER_JAR% from %DOWNLOAD_URL%
powershell -Command "&{Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile %WRAPPER_JAR% -UseBasicParsing}"

:skipDownload
SET "MAVEN_JAVA_EXE=%JAVA_HOME%\bin\java.exe"

IF NOT EXIST "%JAVA_HOME%\" (
    @ECHO Error: JAVA_HOME is not set correctly. >&2
    EXIT /B 1
)

SET "MAVEN_OPTS_TEMP=-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%"
IF NOT "%MAVEN_OPTS%"=="" SET "MAVEN_OPTS_TEMP=%MAVEN_OPTS% %MAVEN_OPTS_TEMP%"
SET "MAVEN_OPTS=%MAVEN_OPTS_TEMP%"

%MAVEN_JAVA_EXE% %JVM_CONFIG_MAVEN_PROPS% %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*

IF ERRORLEVEL 1 GOTO error
GOTO end

:error
SET ERROR_CODE=%ERRORLEVEL%

:end
@endlocal & SET ERROR_CODE=%ERROR_CODE%
IF NOT "%MAVEN_BATCH_PAUSE%"=="on" GOTO end2
ECHO Finished with return value %ERROR_CODE%
PAUSE

:end2
IF "%MAVEN_TERMINATE_CMD%"=="on" EXIT %ERROR_CODE%
CMD /C EXIT /B %ERROR_CODE%
