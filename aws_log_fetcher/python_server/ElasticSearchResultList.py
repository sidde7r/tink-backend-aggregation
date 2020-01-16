from ElasticSearchResultElement import *
from typing import List


class ElasticSearchResultList:

    def __init__(self, data: List[ElasticSearchResultElement]):
        self.data = data
        self.uniqueKeys = set()

        for element in self.data:
            key = element.requestId + "_" + element.credentialsId + "_" + element.providerName
            self.uniqueKeys.add(key)

    def get_unique_keys(self):
        return self.uniqueKeys

    def get_results(self):
        return self.data
