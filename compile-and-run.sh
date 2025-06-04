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
    
    # Run the application
    java -cp "build/classes" com.owsb.Main
else
    echo "Compilation failed!"
    exit 1
fi