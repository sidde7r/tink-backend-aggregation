from ElasticSearchResultElement import *
from typing import List


class ElasticSearchResultList:

    def __init__(self, data: List[ElasticSearchResultElement]):
        self.data = data
        self.reqIDtoUserID = {}
        self.reqIDtoProvider = {}
        self.reqIDtoTimestamp = {}

        """
        Index the result : Map each requestId to userId and providerName so when we query the 
        userId or providerName for a certain requestId it will be easy to answer
        """
        for element in self.data:
            request_id = element.requestId
            user_id = element.userId
            provider_name = element.providerName
            timestamp = element.timestamp

            self.reqIDtoUserID[request_id] = user_id
            self.reqIDtoProvider[request_id] = provider_name
            self.reqIDtoTimestamp[request_id] = timestamp

    def find_user_id_by_request_id(self, request_id):
        return self.reqIDtoUserID[request_id]

    def find_provider_name_by_request_id(self, request_id):
        return self.reqIDtoProvider[request_id]

    def get_timestamp_by_request_id(self, request_id):
        return self.reqIDtoTimestamp[request_id]

    def get_request_ids(self):
        return set(self.reqIDtoUserID.keys())

    def get_results(self):
        return self.data
