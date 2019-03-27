FROM gcr.io/tink-containers/python:3.6-alpine

COPY /requirements.txt /.
RUN pip3 install -r requirements.txt

COPY /cronjob.py /opt/demo_bank/cronjob.py
ENTRYPOINT ["/opt/demo_bank/cronjob.py"]
