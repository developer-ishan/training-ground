#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/src"
OUT="$ROOT/out"

mkdir -p "$OUT"

echo "Compiling..."
find "$SRC" -name "*.java" | xargs javac -d "$OUT"

echo "Running..."
echo ""
java -cp "$OUT" src.Main
