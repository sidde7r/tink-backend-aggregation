
class ElasticSearchResultElement:

    def __init__(self, timestamp, level, logger, message, providerName, userId, clusterId, credentialsId, requestId):
        self.timestamp = timestamp
        self.level = level
        self.logger = logger
        self.message = message
        self.providerName = providerName
        self.userId = userId
        self.clusterId = clusterId
        self.credentialsId = credentialsId
        self.requestId = requestId

    # data = JSON object
    def dict_to_element(data):

        if "message" not in data["_source"]["doc"]:
            message = ""
        else:
            message = data["_source"]["doc"]["message"]

        return ElasticSearchResultElement(data["_source"]["@timestamp"],
                                          data["_source"]["doc"]["level"],
                                          data["_source"]["doc"]["logger"],
                                          message,
                                          data["_source"]["doc"]["mdc"]["providerName"],
                                          data["_source"]["doc"]["mdc"]["userId"],
                                          data["_source"]["doc"]["mdc"]["clusterId"],
                                          data["_source"]["doc"]["mdc"]["credentialsId"],
                                          data["_source"]["doc"]["mdc"]["requestId"])
