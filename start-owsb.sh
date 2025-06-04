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
    
    ant run
else
    echo "Build failed. Please check error messages above."
    exit 1
fi