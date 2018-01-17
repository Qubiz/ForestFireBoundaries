#!/usr/bin/env bash
echo "Compiling proto files..."
protoc --java_out=../../.. header.proto || protoc --java_out=../../.. hotspot-data.proto