@echo off
echo Compiling RoadShieldGUI.java...
javac -encoding UTF-8 -cp "lib/*" RoadShieldGUI.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo Compilation failed! Please check the errors above.
    pause
    exit /b %ERRORLEVEL%
)

echo Starting RoadShield...
java -cp ".;lib/*" RoadShieldGUI
