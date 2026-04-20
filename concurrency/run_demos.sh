#!/bin/bash

# Compile all mini_project files
echo "Compiling mini_project files..."
javac src/main/java/org/example/mini_project/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "Compilation successful!"
echo ""
echo "=========================================="
echo "Choose a demo to run:"
echo "=========================================="
echo "1. Producer-Consumer Demo"
echo "2. Wait/Notify Comparison Demo"
echo "3. Bank Account Demo"
echo "4. Run all demos"
echo "=========================================="
read -p "Enter your choice (1-4): " choice

case $choice in
    1)
        echo -e "\n*** Running Producer-Consumer Demo ***\n"
        java -cp src/main/java org.example.mini_project.ProducerConsumerDemo
        ;;
    2)
        echo -e "\n*** Running Wait/Notify Comparison Demo ***\n"
        java -cp src/main/java org.example.mini_project.WaitNotifyDemo
        ;;
    3)
        echo -e "\n*** Running Bank Account Demo ***\n"
        java -cp src/main/java org.example.mini_project.BankAccountDemo
        ;;
    4)
        echo -e "\n*** Running All Demos ***\n"

        echo -e "\n=== 1. Wait/Notify Comparison Demo ==="
        java -cp src/main/java org.example.mini_project.WaitNotifyDemo

        echo -e "\n\n=== 2. Bank Account Demo ==="
        java -cp src/main/java org.example.mini_project.BankAccountDemo

        echo -e "\n\n=== 3. Producer-Consumer Demo ==="
        java -cp src/main/java org.example.mini_project.ProducerConsumerDemo
        ;;
    *)
        echo "Invalid choice!"
        exit 1
        ;;
esac
