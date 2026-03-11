#!/bin/sh
set -eu

ALIAS_NAME="local"
BUCKET_NAME="${MINIO_BUCKET_NAME:-myblog}"
POLICY_FILE="/tmp/public-read-policy.json"

cat > "${POLICY_FILE}" <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowAnonymousBucketMeta",
      "Effect": "Allow",
      "Principal": {
        "AWS": ["*"]
      },
      "Action": [
        "s3:GetBucketLocation",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::${BUCKET_NAME}"
      ]
    },
    {
      "Sid": "AllowAnonymousObjectRead",
      "Effect": "Allow",
      "Principal": {
        "AWS": ["*"]
      },
      "Action": [
        "s3:GetObject"
      ],
      "Resource": [
        "arn:aws:s3:::${BUCKET_NAME}/*"
      ]
    }
  ]
}
EOF

until mc alias set "${ALIAS_NAME}" "http://minio:9000" "${MINIO_ROOT_USER}" "${MINIO_ROOT_PASSWORD}"; do
  echo "Waiting for MinIO to become ready..."
  sleep 2
done

mc mb --ignore-existing "${ALIAS_NAME}/${BUCKET_NAME}"
mc anonymous set-json "${POLICY_FILE}" "${ALIAS_NAME}/${BUCKET_NAME}"
mc anonymous get-json "${ALIAS_NAME}/${BUCKET_NAME}"
