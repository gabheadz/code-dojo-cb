#!/bin/bash

# run mocks using mockoon cli, if not installed please run:
#
# npm install -g @mockoon/cli
#

mockoon-cli start --data ./alfa_notification_api.json &
mockoon-cli start --data ./beta_notification_api.json &
