#!/bin/bash
# Setup script for gRPC Python example

echo "Setting up gRPC Python example..."
echo ""

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "Error: Python 3 is not installed"
    exit 1
fi

# Check if pip is installed
if ! command -v pip3 &> /dev/null; then
    echo "Error: pip3 is not installed"
    exit 1
fi

# Install dependencies
echo "Installing Python dependencies..."
pip3 install grpcio grpcio-tools

# Generate Python code from proto file
echo ""
echo "Generating Python code from user.proto..."
python3 -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. user.proto

if [ $? -eq 0 ]; then
    echo ""
    echo "Setup complete! Generated files:"
    echo "  - user_pb2.py"
    echo "  - user_pb2_grpc.py"
    echo ""
    echo "You can now run:"
    echo "  Server: python3 server.py"
    echo "  Client: python3 client.py"
else
    echo ""
    echo "Error generating code from proto file"
    exit 1
fi

