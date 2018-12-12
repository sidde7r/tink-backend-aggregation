FROM gcr.io/tink-containers/python:3.6-alpine

COPY /requirements.txt /.
RUN pip3 install -r requirements.txt

COPY /cronjob.py /opt/provider_status/cronjob.py
ENTRYPOINT ["/opt/provider_status/cronjob.py"]
