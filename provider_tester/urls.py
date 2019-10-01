from config import AggregationConfig


class URLConfig:

    client_token_fetch_url = "oauth/token"
    user_create_request_url = "user/create"
    grant_user_access_url = "oauth/authorization-grant"
    providers_url = "providers"
    create_credentials_url = "credentials"
    credentials_status_url = "credentials/<credentials_id>"
    refresh_credentials_url = "credentials/<credentials_id>/refresh"
    accounts_url = "accounts/list"
    bypass_filters_url = "user/psd2flag"

    def __init__(self):
        pass

    @staticmethod
    def get_client_token_fetch_url():
        return AggregationConfig.host_url + URLConfig.client_token_fetch_url

    @staticmethod
    def get_user_create_request_url():
        return AggregationConfig.host_url + URLConfig.user_create_request_url

    @staticmethod
    def get_grant_user_access_request_url():
        return AggregationConfig.host_url + URLConfig.grant_user_access_url

    @staticmethod
    def get_providers_url():
        return AggregationConfig.host_url + URLConfig.providers_url

    @staticmethod
    def get_create_credentials_url():
        return AggregationConfig.host_url + URLConfig.create_credentials_url

    @staticmethod
    def get_credentials_status_url(credentials_id):
        return AggregationConfig.host_url + URLConfig.credentials_status_url.replace("<credentials_id>", credentials_id)

    @staticmethod
    def get_credentials_refresh_url(credentials_id):
        return AggregationConfig.host_url + URLConfig.refresh_credentials_url.replace("<credentials_id>", credentials_id)

    @staticmethod
    def get_accounts_list_url():
        return AggregationConfig.host_url + URLConfig.accounts_url

    @staticmethod
    def get_bypass_filters_url():
        return AggregationConfig.host_url + URLConfig.bypass_filters_url

