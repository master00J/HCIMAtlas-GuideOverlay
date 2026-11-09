@echo off
cd /d "%~dp0"
if not defined JAVA_HOME if exist "C:\Program Files\Eclipse Adoptium\jdk-11.0.33.10-hotspot\bin\java.exe" (
  set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.33.10-hotspot"
)
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

netstat -ano | findstr ":38877" | findstr "LISTENING" >nul
if errorlevel 1 (
  echo Streamserver starten...
  start "HCIM Atlas Overlay" /min cmd /k "cd /d %USERPROFILE%\Documents\Atlas-HCIM && npm start"
  timeout /t 2 /nobreak >nul
)

echo HCIM Atlas RuneLite starten...
call gradlew.bat runAtlas
pause
