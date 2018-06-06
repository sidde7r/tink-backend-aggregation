import sys
import argparse
from gevent.pywsgi import WSGIServer
from flask import Flask, jsonify, request

USE_STDIN = False
try:
    from Tkinter import *
except:
    print >> sys.stderr, "TKinter was not installed, will use stdin instead!"
    USE_STDIN = True

app = Flask(__name__)


def get_description(field, separator="\n"):
    desc = field.get("description").encode("utf-8")
    if field.get("helpText"):
        desc += separator + "(%s)" % field.get("helpText").encode("utf-8")

    return desc

class SupplementalPopup(object):
    def __init__(self, fields):
        self.window = Tk()
        self.fields = fields
        self.answers = {}

        self.create_gui_fields()
        self.window.bind("<Return>", self.cleanup)
        button_cancel = Button(self.window, text="Cancel", command=self.destroy)
        button_cancel.pack(side=LEFT, padx=5, pady=5)
        button_send = Button(self.window, text="Send", command=self.cleanup)
        button_send.pack(side=LEFT, padx=5, pady=5)
        self.window.mainloop()

    def get_answers(self):
        return self.answers

    def create_gui_fields(self):
        val_numeric = (self.window.register(self.validate_numerics), "%S")

        self.gui_fields = []
        for field in self.fields:
            row = Frame(self.window)
            label = Label(row, width=15, text=get_description(field), anchor="w")

            options = {}
            if field.get("masked"):
                options["show"] = "*"

            if field.get("numeric"):
                options["validate"] = "key"
                options["validatecommand"] = val_numeric

            input = Entry(row, **options)
            if field.get("value"):
                input.delete(0, END)
                input.insert(0, field.get("value"))

            row.pack(side=TOP, fill=X, padx=5, pady=5)
            label.pack(side=LEFT)
            input.pack(side=RIGHT, expand=YES, fill=X)
            self.gui_fields.append((field.get("name"), input))

    def validate_numerics(self, text):
        return text in "0123456789"

    def destroy(self):
        self.window.destroy()

    def cleanup(self):
        for entry in self.gui_fields:
            key = entry[0]
            value = entry[1].get()

            self.answers[key] = value

        self.destroy()


class SupplementalStdin(object):
    def __init__(self, fields):

        self.answers = {}
        print("-" * 20)
        for field in fields:
            key = field.get("name")
            desc = get_description(field, " ")
            if field.get("value"):
                value = field.get("value").encode("utf-8")
                print("%s: %s" % (desc, value))
            else:
                value = raw_input("%s: " % desc)

            self.answers[key] = value
        print("-" * 20)

    def get_answers(self):
        return self.answers


# fields are a key:value map. This function returns the same type of
# data with values, or None if cancelled.
def ask_user_for_data(fields):
    if USE_STDIN:
        sp = SupplementalStdin(fields)
    else:
        sp = SupplementalPopup(fields)
    return sp.get_answers()


@app.route("/api/v1/supplemental", methods=("POST",))
def supplemental():
    if not request.json:
        return None

    fields = request.get_json()
    answers = ask_user_for_data(fields)
    return jsonify(answers)


def main():
    global USE_STDIN

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
            "-s",
            "--stdin",
            action="store_true",
            help="use stdin"
    )
    args = parser.parse_args()

    if args.stdin:
        USE_STDIN = True

    http_server = WSGIServer((args.bind, args.port), app, log=None)
    http_server.serve_forever()

    return 0

if __name__ == "__main__":
    sys.exit(main())
