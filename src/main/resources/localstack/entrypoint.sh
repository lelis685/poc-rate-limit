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

create_s3() {
    echo "configuring s3"
    awslocal s3api create-bucket --bucket "bucket-cadastro-cliente-config-rate-limit" --region ${AWS_REGION}
    echo "configured s3"
    echo "==================="
}

populate_s3() {
    echo "loading files s3"
    ls
    awslocal s3 cp /s3-data/ s3://bucket-cadastro-cliente-config-rate-limit --recursive
    echo "loaded files s3"
    echo "==================="
}

create_queue "sqs-cadastro-cliente"
create_s3
populate_s3