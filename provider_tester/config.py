
class AggregationConfig:

    host_url = "https://api.tink.se/api/v1/"

    client_token_request_data = {
        "client_id": "<client_id>",
        "client_secret": "<client_secret>",
        "grant_type": "client_credentials",
        "scope": "authorization:grant,user:create,user:write"
    }

    grant_user_access_request_data = {
        "user_id": "<user_id>",
        "scope": "accounts:read,transactions:read,user:read,credentials:read,credentials:refresh,credentials:write,user:write"
    }

    @staticmethod
    def get_grant_user_access_request_payload(user_id):
        payload = dict(AggregationConfig.grant_user_access_request_data)
        payload["user_id"] = user_id
        return payload

    @staticmethod
    def get_exchange_token_request_payload(user_code):
        payload = dict(AggregationConfig.client_token_request_data)
        payload["code"] = user_code
        payload["grant_type"] = "authorization_code"
        return payload
