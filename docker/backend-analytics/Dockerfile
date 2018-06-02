FROM tink/base-java:8

ENV SERVICE_DIR=/usr/share/tink-backend-analytics

COPY tools ${SERVICE_DIR}/tools
COPY data/analytics-jobs ${SERVICE_DIR}/data/analytics-jobs
COPY data/scripts/analytics-jobs ${SERVICE_DIR}/data/scripts/analytics-jobs
COPY data/seeding/cities-se.txt ${SERVICE_DIR}/data/seeding/
COPY data/seeding/cities-nl.txt ${SERVICE_DIR}/data/seeding/
COPY data/scripts/tink-spark-submit.py /usr/local/bin/

COPY src/analytics-jobs/target/lib ${SERVICE_DIR}/lib
COPY src/analytics-jobs/target/analytics-jobs.jar ${SERVICE_DIR}/
