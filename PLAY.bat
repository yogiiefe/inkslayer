@echo off
setlocal
set "INKSLAYER_JAVA="
if exist "%JAVA_HOME%\bin\java.exe" set "INKSLAYER_JAVA=%JAVA_HOME%\bin\java.exe"
for /d %%J in ("%ProgramFiles%\Java\jdk-21*") do if exist "%%~J\bin\java.exe" set "INKSLAYER_JAVA=%%~J\bin\java.exe"
if not defined INKSLAYER_JAVA set "INKSLAYER_JAVA=java"
if not exist "%~dp0lwjgl3\build\libs\INKSLAYER!-1.0.0.jar" (
  echo Build the game first. See README.md for Java 21 and Gradle instructions.
  pause
  exit /b 1
)
"%INKSLAYER_JAVA%" -jar "%~dp0lwjgl3\build\libs\INKSLAYER!-1.0.0.jar"
if errorlevel 1 (
  echo INKSLAYER requires Java 21 or newer. See README.md.
  pause
)
endlocal
