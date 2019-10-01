
class ClientTokenResponse:

    def __init__(self, data):
        self.expires_in = data["expires_in"]
        self.access_token = data["access_token"]
        self.scope = data["scope"]
