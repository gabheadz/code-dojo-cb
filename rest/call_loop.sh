#!/bin/bash

# URL to make the curl request to
URL="http://localhost:8080/api/money/send"

# souce account from argument $1 or a default value
CTA1="${1:-"23123123199"}"

# destination account from argument $2 or a default value
CTA2="${2:-"98756543299"}"

BODY="{\"originAccount\": "${CTA1}", \"destinationAccount\": "${CTA2}",  \"amount\": 1000.0}"

# Infinite loop to make the curl request every 2 seconds
while true; do

    # Make the curl request
    curl --request POST \
    --url "$URL" \
    --header 'Content-Type: application/json' \
    --data "$BODY"

    echo "\n"
    
    # Wait for 2 seconds
    sleep 2
done