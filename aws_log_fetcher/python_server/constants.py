find_aws_log_link_query = {
    "bool": {
        "must": [
            {
                "match_all": {}
            },
            {
                "match_phrase": {
                    "doc.mdc.requestId": {
                        "query": "<requestId>"
                    }
                }
            },
            {
                "match_phrase": {
                    "doc.mdc.credentialsId": {
                        "query": "<credentialsId>"
                    }
                }
            },
            {
                "match_phrase": {
                    "doc.mdc.providerName": {
                        "query": "<providerName>"
                    }
                }
            },
            {
                "range": {
                    "@timestamp": {
                        "gte": "<gte>",
                        "lte": "<lte>",
                        "format": "strict_date_optional_time"
                    }
                }
            }
        ],
        "filter": [],
        "should": [],
        "must_not": []
    }
}

header = {
    "Content-Type": "application/x-ndjson",
    "Accept": "application/json, text/plain, */*",
    "cookie": "<cookie>",
    "kbn-version": "7.3.2",
    "origin": "https://kibana.aggregation-production.tink.network",
    "referer": "https://kibana.aggregation-production.tink.network/app/kibana",
    "user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36"
}

request_template = "{\"version\":true,\"size\":500,\"sort\":[{\"@timestamp\":{\"order\":\"desc\",\"unmapped_type\":\"boolean\"}}],\"_source\":{\"excludes\":[]},\"aggs\":{\"2\":{\"date_histogram\":{\"field\":\"@timestamp\",\"interval\":\"12h\",\"time_zone\":\"Europe/Berlin\",\"min_doc_count\":1}}},\"stored_fields\":[\"*\"],\"script_fields\":{},\"docvalue_fields\":[{\"field\":\"@timestamp\",\"format\":\"date_time\"},{\"field\":\"doc.@timestamp\",\"format\":\"date_time\"},{\"field\":\"doc.time\",\"format\":\"date_time\"},{\"field\":\"doc.time_iso8601\",\"format\":\"date_time\"}],\"query\":<query>,\"highlight\":{\"pre_tags\":[\"@kibana-highlighted-field@\"],\"post_tags\":[\"@/kibana-highlighted-field@\"],\"fields\":{\"*\":{}},\"fragment_size\":2147483647}}"
