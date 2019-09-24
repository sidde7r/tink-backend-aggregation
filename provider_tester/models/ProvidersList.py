
class ProvidersList:

    def __init__(self, data):
        providers = data["providers"]
        self.providers = {}
        for provider in providers:
            self.providers[provider["name"]] = provider

    def get_provider_data(self, name):
        if name not in self.providers.keys():
            print("[ERROR] Provider " + name
                  + " is not among available providers. Available providers are the following : ")
            print(",".join(list(self.providers.keys())))
            raise Exception("[ERROR] Tried to use invalid provider, test failed")

        return self.providers[name]
