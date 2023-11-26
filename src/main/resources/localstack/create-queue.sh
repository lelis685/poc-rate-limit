#!/usr/bin/env bash

set -euo pipefail


AWS_REGION=us-east-1

create_queue() {
    local QUEUE_NAME_TO_CREATE=$1
    echo "configuring sqs"
    awslocal sqs create-queue --queue-name ${QUEUE_NAME_TO_CREATE} --region ${AWS_REGION}
    echo "configured sqs ${QUEUE_NAME_TO_CREATE}"
    echo "==================="
}

create_queue "sqs-cadastro-cliente"
