#!/bin/bash

echo "OWSB - Compiling and Running..."

cd "$(dirname "$0")"

# Create build directory
mkdir -p build/classes

# Compile all Java files
echo "Compiling Java files..."
find src -name "*.java" -exec javac -d build/classes -cp "build/classes" {} +

if [ $? -eq 0 ]; then
    echo ""
    echo "Compilation successful!"
    echo ""
    echo "Starting OWSB application..."
    echo "Login: admin / Admin@123"
    echo ""
    
    # Run the application, using a virtual display if needed
    if [ -z "$DISPLAY" ]; then
        if command -v xvfb-run >/dev/null 2>&1; then
            echo "No DISPLAY found, launching with xvfb-run..."
            xvfb-run -a java -cp "build/classes" com.owsb.Main
        else
            echo "Error: DISPLAY not set and xvfb-run is missing."
            echo "Please install Xvfb or run inside a graphical environment."
            exit 1
        fi
    else
        java -cp "build/classes" com.owsb.Main
    fi
else
    echo "Compilation failed!"
    exit 1
fi
