@echo off
REM Simple run script for WikiViewer on Windows

REM Create bin directory
if not exist bin mkdir bin

REM Compile the project
echo Compiling...
javac -cp "libs/*;src" -d bin src/gr/eap/wikiviewer/model/*.java src/gr/eap/wikiviewer/service/*.java src/gr/eap/wikiviewer/gui/*.java

REM Copy resources
echo Copying resources...
xcopy /E /I /Y src\META-INF bin\META-INF

REM Run the application
echo Running...
java -cp "bin;libs/*" gr.eap.wikiviewer.gui.MainFrame
pause
