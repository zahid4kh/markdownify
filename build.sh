#!/bin/bash

chmod +x gradlew
clear && echo "Cleaning the build folder"
./gradlew clean

echo "Building..." 
./gradlew build
