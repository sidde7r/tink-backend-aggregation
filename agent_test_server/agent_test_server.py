from __future__ import print_function
from builtins import input
from builtins import object
import sys
import argparse
import os
from flask import Flask, jsonify, request, abort, Response, redirect
from functools import wraps
import webbrowser

from memqueue import MemoryMessageQueue

queue = MemoryMessageQueue()

app = Flask(__name__)

CREDENTIALS_PATH = os.path.dirname(os.path.realpath(__file__)) + "/credentials/"


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
                value = input("%s: " % desc)

            self.answers[key] = value
        print("-" * 20)

    def get_answers(self):
        return self.answers

    @staticmethod
    def get_description(field):
        desc = ''
        if field.get("description"):
            desc += field.get("description").encode("utf-8")
        if field.get("helpText"):
            desc += " (%s)" % field.get("helpText").encode("utf-8")

        return desc


# fields are a key:value map. This function returns the same type of
# data with values, or None if cancelled.
def ask_user_for_data(fields):
    sp = SupplementalStdin(fields)
    return sp.get_answers()


def sanitize_filename(filename):
    return "".join([c for c in filename if c.isalpha() or c.isdigit() or c == " "]).rstrip()


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

    return ("", 204)


@app.route("/api/v1/credential/<provider>/<credentialId>", methods=("GET",))
def load_credential(provider, credentialId):
    filename = CREDENTIALS_PATH + sanitize_filename(provider + credentialId)

    try:
        with open(filename, "rb") as f:
            return Response(f.read(), mimetype="application/json")
    except IOError:
        abort(404, "credential not found")


@app.route("/api/v1/supplemental/<key>", methods=("POST",))
def request_supplemental(key):
    if not request.json:
        return None

    fields = request.get_json()

    answers = ask_user_for_data(fields)

    # Put the answers on the queue so that it can be picked up
    # when the agent asks for the supplemental information.
    queue.put(key, answers)

    return ("", 204)


@app.route("/api/v1/supplemental/<key>/<timeout_seconds>", methods=("GET",))
def get_supplemental(key, timeout_seconds):
    answers = queue.get(key, int(timeout_seconds))
    return jsonify(answers)


# only support web browser as app.
@app.route("/api/v1/thirdparty/open", methods=("POST",))
def thirdparty_open():
    def get_ios_url(payload):
        return payload.get("ios", {}).get("deepLinkUrl", None)

    if not request.json:
        return None

    payload = request.get_json()

    url = get_ios_url(payload)
    if not url:
        return ("invalid payload", 500)

    webbrowser.open_new_tab(url)

    return ("", 204)


# This endpoint will be accessed/opened by the web browser as a result
# of a redirect from the bank's backend.
@app.route("/api/v1/thirdparty/callback", methods=("GET", "POST"))
@app.route("/api/v1/credentials/third-party/callback", methods=("GET", "POST"))
def thirdparty_callback():
    args = request.args or request.form

    state = args.get("state", None)
    if not state:
        if request.method == "POST":
            abort(400, "invalid request")

        # No state means that it was an openid request (# instead of ?
        # in the url).
        # We must return a small script to the browser to send the
        # parameters as a POST to this endpoint.
        return """<html>
    <head>
        <script>
            var path = window.location.pathname;
            var data = window.location.hash.substr(1);
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if(this.readyState == XMLHttpRequest.DONE && this.status == 200) {
                    window.location.replace(this.responseText);
                }
            };
            xhr.open("POST", path, true);
            xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
            xhr.send(data);
        </script>
    </head>
</html>"""

    # turn it into a dict from a ImmutableMultiDict (we don't expect or
    # support lists)
    parameters = {k: v for (k, v) in args.items()}

    # Put the parameters on the queue so that it can be picked up
    # when the agent asks for the supplemental information.
    queue.put("tpcb_%s" % state, parameters)

    if request.method == "GET":
        return redirect("tink://open", 302)
    return "tink://open"


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

    app.run(threaded=True, ssl_context="adhoc", host=args.bind, port=args.port, load_dotenv=False)
    return 0


if __name__ == "__main__":
    sys.exit(main())
