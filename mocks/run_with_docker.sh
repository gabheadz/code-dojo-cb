#!/bin/bash

check_container () {
    # Check if the container exists
    if docker inspect "$1" > /dev/null 2>&1; then
        echo "The container $1 exists."
        
        # Check if the container is running
        if $(docker inspect -f '{{.State.Status}}' "$1" | grep -q "running"); then
            echo "The container $1 is running."
        else
            echo "The container $1 is not running."
            
            # Start the container if it is not running
            docker start "$1"
        fi
    else
        echo "The container $1 does not exist."
        
        # Create and start the container if it does not exist
        docker run -d --name "$1" --mount type=bind,source=./"$2",target=/data,readonly -p "$3":"$3" mockoon/cli:latest -d data -p "$3" 
    fi
}


# exposes mock alfa on port 3001
check_container mock_alfa alfa_notification_api.json 3001

# exposes mock beta on port 3002
check_container mock_beta beta_notification_api.json 3002
