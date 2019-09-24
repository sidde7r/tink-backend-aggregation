from models.ClientTokenResponse import ClientTokenResponse
import json
import requests


class HTTPRequestHelper:

    def __init__(self, token_data: ClientTokenResponse = None):
        self.token_data = token_data
        if token_data is not None:
            print("[DEBUG]: Token = " + token_data.access_token)

    """
    payload: None or dict
    header: None or dict
    query_params: None or dict
    request_type: "GET" or "POST"
    """
    def make_request(self, url, payload=None, header=None, stringify_payload=False, content_type=None,
                     query_params=None, request_type="POST"):

        if request_type is "GET" and payload is not None:
            raise Exception("[ERROR] Cannot have payload when making GET request")

        if query_params is not None:
            url = url + "?" + "&".join([(key + "=" + value) for key, value in query_params.items()])

        if stringify_payload and payload is None:
            raise Exception("[ERROR] Payload cannot be null when stringify_payload flag is set to True")

        if payload is not None:
            if stringify_payload:
                payload_to_send = "&".join([(key + "=" + value) for key, value in payload.items()])
            else:
                payload_to_send = json.dumps(payload)
        else:
            payload_to_send = None

        if header is None:
            header_to_send = dict()
        else:
            header_to_send = dict(header)

        if content_type is not None and request_type is not "PUT":
            header_to_send["Content-Type"] = content_type

        if self.token_data is not None:
            header_to_send["Authorization"] = "Bearer " + self.token_data.access_token

        if request_type is "POST":
            req = requests.post(url, data=payload_to_send, headers=header_to_send)
        elif request_type is "GET":
            req = requests.get(url, headers=header_to_send)
        elif request_type is "PUT":
            req = requests.put(url, headers=header_to_send)
        else:
            raise Exception("[ERROR] Unknown request type " + request_type)

        # Sometimes we are getting timeout error, for this reason we need to do the check
        if req.status_code != 200 and req.status_code != 204:
            raise Exception("[ERROR] An error occurred during HTTP request")

        if len(req.text) > 0:
            return json.loads(req.text)
        else:
            return None
