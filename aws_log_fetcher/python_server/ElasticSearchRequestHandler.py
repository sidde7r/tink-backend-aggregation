import requests
import json
from ElasticSearchResultList import *
from constants import header, request_template


class ElasticSearchRequestHandler:

    # When doing a request to ElasticSearch, we always have to add this to the payload
    request_prefix = {
        "index": "logstash*",
        "ignore_unavailable": True,
        "preference": 1573550270374
    }

    def __init__(self, cookie, host):
        self.cookie = cookie
        self.headers = json.loads(json.dumps(header).replace("<cookie>", cookie))
        self.request_template = request_template
        self.main_url = host + "/elasticsearch/_msearch?rest_total_hits_as_int=true&ignore_throttled=true"

    def should_process(self, element):
        if "_source" not in element:
            return False
        if "doc" not in element["_source"]:
            return False
        if "mdc" not in element["_source"]["doc"]:
            return False
        if "providerName" not in element["_source"]["doc"]["mdc"]:
            return False
        return True

    def make_query(self, query, replacements={}) -> ElasticSearchResultList:

        for key, value in replacements:
            query = query.replace(key, value)

        # Create the request payload by injecting query and prepending the prefix payload
        request = self.request_template.replace("<query>", query)
        request = json.dumps(ElasticSearchRequestHandler.request_prefix) + "\n" + request + "\n"

        # Make the query get JSON string as response
        elasticsearch_request = requests.post(self.main_url,
                                              headers=self.headers,
                                              data=request)

        elasticsearch_response = json.loads(elasticsearch_request.text)
        results = elasticsearch_response["responses"][0]["hits"]["hits"]

        # Convert results into an array of ElasticSearchResultElement objects
        final_results = ElasticSearchResultList(
            [ElasticSearchResultElement.dict_to_element(element) for element in results if self.should_process(element)]
        )
        return final_results
