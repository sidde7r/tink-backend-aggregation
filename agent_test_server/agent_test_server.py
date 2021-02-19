from __future__ import print_function, unicode_literals

import argparse
import os
import sys
import webbrowser

from cacheout import Cache
from flask import Flask, jsonify, request, abort, Response, redirect

from bankid import generate_bankid_qrcode
from memqueue import MemoryMessageQueue
from supplemental_stdin import SupplementalStdin

cache = Cache()
queue = MemoryMessageQueue()

app = Flask(__name__, static_url_path='/static')

CREDENTIALS_PATH = os.path.dirname(os.path.realpath(__file__)) + "/credentials/"


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

    return "", 204


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

    return "", 204


@app.route("/api/v1/supplemental/<key>/<timeout_seconds>", methods=("GET",))
def get_supplemental(key, timeout_seconds):
    answers = queue.get(key, int(timeout_seconds))
    return jsonify(answers)


@app.route("/api/v1/provider-session-cache/<key>/<timeout_seconds>", methods=("POST",))
def request_provider_session_cache(key, timeout_seconds):
    if not request.json:
        return None

    value = request.get_json()

    # Put the answers on the cache so that it can be picked up
    # when the agent asks for the provider session cache.
    cache.set(key, value, int(timeout_seconds))

    return "", 204


@app.route("/api/v1/provider-session-cache/<key>", methods=("GET",))
def get_provider_session_cache(key):
    answers = cache.get(key)
    return jsonify(answers)


# only support web browser as app.
@app.route("/api/v1/thirdparty/open", methods=("POST",))
def thirdparty_open():
    def get_ios_url(payload):
        return payload.get("ios", {}).get("deepLinkUrl", None)

    if not request.json:
        return None

    json_payload = request.get_json()

    url = get_ios_url(json_payload)
    if not url:
        return "invalid payload", 500

    webbrowser.open_new_tab(url)

    return "", 204


def verify_state_parameter(state):
    # This logic can be improved to verify that it's an actual uuid.
    # Note: The code in our main /callback endpoint has more
    # logic handling the uuid and should be used as a source for this
    # work. Some banks remove the dashes in the uuid for example (!).

    uuid_tag = "feed"
    return state.lower().endswith(uuid_tag)


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

    if not verify_state_parameter(state):
        print("[!] Invalid state parameter: ", state)
        print("[!] Possible reasons:")
        print("[!]   - The agent has generated its own state.")
        print("[!]   - The bank has modified the original state.")
        return "", 400

    # turn it into a dict from a ImmutableMultiDict (we don't expect or
    # support lists)
    parameters = {k: v for (k, v) in list(args.items())}

    # Put the parameters on the queue so that it can be picked up
    # when the agent asks for the supplemental information.
    queue.put("tpcb_%s" % state, parameters)

    if request.method == "GET":
        return redirect("tink://open", 302)
    return "tink://open"


autoStartToken = None


@app.route("/bankid/bankid-iframe.html", methods=("GET",))
def bankid_iframe_page():
    return app.send_static_file("bankid-iframe.html")


@app.route("/bankid", methods=("GET",))
def bankid_page():
    return app.send_static_file("bankid.html")


@app.route("/api/v1/bankid/poll", methods=("GET",))
def bankid_poll():
    global autoStartToken
    currentToken = None
    if autoStartToken is not None:
        currentToken = autoStartToken
        autoStartToken = None
        print("Sending autostart token: '" + currentToken + "'")

    return jsonify({"token": currentToken, "image": generate_bankid_qrcode(currentToken)})


@app.route("/api/v1/bankid/send/<token>", methods=("POST",))
def bankid_send_autostart(token):
    global autoStartToken
    autoStartToken = token
    return jsonify({})


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
    parser.add_argument(
        "-c",
        "--cert",
        help="SSL certificate to use"
    )
    parser.add_argument(
        "-k",
        "--key",
        help="Private key for SSL certificate"
    )
    args = parser.parse_args()

    if (args.cert and not args.key) or (args.key and not args.cert):
        print("Both certificate and key must be passed.")
        sys.exit(1)

    if (args.cert and args.key):
        sslContext = (args.cert, args.key)
    else:
        sslContext = "adhoc"

    app.run(threaded=True, ssl_context=sslContext, host=args.bind, port=args.port, load_dotenv=False)
    return 0


if __name__ == "__main__":
    sys.exit(main())
