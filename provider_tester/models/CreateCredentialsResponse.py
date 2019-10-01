

class CreateCredentialsResponse:

    def __init__(self, data):
        self.credentials_id = data["id"]
        self.status = data["status"]
        self.user_id = data["userId"]
