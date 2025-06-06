#!/bin/bash

echo "=========================================="
echo "  OWSB Purchase Order Management System  "
echo "=========================================="
echo ""

cd "$(dirname "$0")"

echo "Building application..."
ant clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "Starting OWSB application..."
    echo ""
    echo "Login Credentials:"
    echo "  Username: admin"
    echo "  Password: Admin@123"
    echo ""
    echo "Close this terminal window to stop the application."
    echo ""
    
    if [ -z "$DISPLAY" ]; then
        if command -v xvfb-run >/dev/null 2>&1; then
            echo "No DISPLAY found, launching with xvfb-run..."
            xvfb-run -a ant run
        else
            echo "Error: DISPLAY not set and xvfb-run is missing."
            echo "Please install Xvfb or run inside a graphical environment."
            exit 1
        fi
    else
        ant run
    fi
else
    echo "Build failed. Please check error messages above."
    exit 1
fi
