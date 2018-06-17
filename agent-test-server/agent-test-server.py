import sys
import argparse
import os
from gevent.pywsgi import WSGIServer
from flask import Flask, jsonify, request, abort, Response

app = Flask(__name__)

CREDENTIALS_PATH = "./credentials/"

class SupplementalStdin(object):
    def __init__(self, fields):
        self.answers = {}
        print("-" * 20)
        for field in fields:
            key = field.get("name")
            desc = self.get_description(field)
            if field.get("value"):
                value = field.get("value").encode("utf-8")
                print("%s: %s" % (desc, value))
            else:
                value = raw_input("%s: " % desc)

            self.answers[key] = value
        print("-" * 20)

    def get_answers(self):
        return self.answers

    def get_description(self, field, separator=" "):
        desc = field.get("description").encode("utf-8")
        if field.get("helpText"):
            desc += separator + "(%s)" % field.get("helpText").encode("utf-8")

        return desc


# fields are a key:value map. This function returns the same type of
# data with values, or None if cancelled.
def ask_user_for_data(fields):
    sp = SupplementalStdin(fields)
    return sp.get_answers()


def sanitize_filename(filename):
    return "".join([c for c in filename if c.isalpha() or c.isdigit() or c==" "]).rstrip()


def make_dirs(path):
    try:
        os.makedirs(path)
    except OSError:
        if not os.path.isdir(path):
            raise


@app.route("/api/v1/credential/<provider>/<credentialId>", methods=("POST",))
def save_credential(provider, credentialId):

    # make sure the credentials output path exists
    make_dirs(CREDENTIALS_PATH)

    filename = CREDENTIALS_PATH + sanitize_filename(provider + credentialId)

    with open(filename, "wb") as f:
        f.write(request.get_data())

    return ('', 204)


@app.route("/api/v1/credential/<provider>/<credentialId>", methods=("GET",))
def load_credential(provider, credentialId):
    filename = CREDENTIALS_PATH + sanitize_filename(provider + credentialId)

    try:
        with open(filename, "rb") as f:
            return Response(f.read(), mimetype="application/json")
    except IOError:
        abort(404, "credential not found")


@app.route("/api/v1/supplemental", methods=("POST",))
def supplemental():
    if not request.json:
        return None

    fields = request.get_json()
    answers = ask_user_for_data(fields)
    return jsonify(answers)


def main():
    parser = argparse.ArgumentParser(description="Agent test server")
    parser.add_argument(
            "-p",
            "--port",
            type=int,
            default=7357,
            help="web server port"
    )
    parser.add_argument(
            "-b",
            "--bind",
            default="127.0.0.1",
            help="ip to bind on"
    )
    args = parser.parse_args()

    print("Agent test server listening on %s:%u" % (args.bind, args.port))

    http_server = WSGIServer((args.bind, args.port), app, log=None)
    http_server.serve_forever()

    return 0


if __name__ == "__main__":
    sys.exit(main())
