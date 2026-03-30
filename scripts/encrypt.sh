#!/bin/bash
# Jasypt Encryption Utility for Pharmacy Platform
# Usage: ./encrypt.sh "plaintext_password"
# Requires JASYPT_ENCRYPTOR_PASSWORD environment variable

if [ -z "$JASYPT_ENCRYPTOR_PASSWORD" ]; then
    echo "Error: JASYPT_ENCRYPTOR_PASSWORD environment variable not set"
    exit 1
fi

if [ -z "$1" ]; then
    echo "Usage: $0 \"plaintext_password\""
    exit 1
fi

PASSWORD="$1"
ALGORITHM="PBEWithMD5AndDES"

# Use openssl for simple encryption (demo purposes)
# In production, use jasypt CLI tool

ENCRYPTED=$(echo -n "$PASSWORD" | openssl enc -$ALGORITHM -A -a -S "$(openssl rand -hex 8)" -k "$JASYPT_ENCRYPTOR_PASSWORD" 2>/dev/null)

if [ $? -eq 0 ]; then
    echo "ENC($ENCRYPTED)"
else
    echo "Error: Encryption failed"
    exit 1
fi
