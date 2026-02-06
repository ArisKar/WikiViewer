#!/bin/bash
# Simple run script for WikiViewer

# Compile the project
mkdir -p bin
CP="libs/*:src"
javac -cp "$CP" -d bin $(find src -name "*.java")

# Copy resources
cp -r src/META-INF bin/

# Run the application
java -cp "bin:libs/*" gr.eap.wikiviewer.gui.MainFrame
