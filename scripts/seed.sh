#!/usr/bin/env bash
# Re-creates the dataset: deletes all employees and seeds a fresh, deterministic set.
# Runs the app without a web server, so it exits when seeding is done.
#
#   scripts/seed.sh            # 10,000 employees into backend/salary.db
#   scripts/seed.sh 50000      # a bigger dataset, e.g. for performance checks
#   DB_PATH=/data/salary.db scripts/seed.sh
set -euo pipefail

COUNT="${1:-10000}"
cd "$(dirname "$0")/../backend"

./mvnw -q -B spring-boot:run \
  -Dspring-boot.run.arguments="--spring.main.web-application-type=none --app.seed.reset=true --app.seed.employee-count=${COUNT}"
