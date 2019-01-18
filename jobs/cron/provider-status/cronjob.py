#!/usr/bin/env python3

import requests, json, logging, sys, getopt, os
from collections import defaultdict

# STATUS PAGE
STATUSPAGE_API_KEY = os.environ.get("STATUSPAGE_API_KEY").rstrip("\n")
STATUSPAGE_API_BASE = "https://api.statuspage.io/v1/pages/"
NOT_CIRCUIT_BROKEN = 0
PAGE_ID = "x1lbt12g0ryw"
COMPONENTS_PATH = "/components/"

STATUS_ENUMS = {
    0.50: "degraded_performance",
    0.75: "partial_outage",
    1: "major_outage"
}

GROUP_IDS = {
    "se": "c20kyrkjrgks"
}

# PROMETHEUS
PROMETHEUS_API_BASE = "http://prometheus.monitoring-prometheus.svc.cluster.local:9090/api/v1/query"

### Queries
PROVIDERS_QUERY = "sum(tink_circuit_broken_providers{cluster='aggregation', environment='production', provider!~'.*abstract.*', className!~'abnamro.*|demo.DemoAgent|nxgen.demo.*|fraud.CreditSafeAgent'}) by (provider, market)"
INSTANCES_QUERY = "sum(up{job='tink-aggregation', environment='production'})"

# LOGGING
logger = logging.getLogger('statuspage_provider_status')
logger.setLevel(logging.DEBUG)
console_handler = logging.StreamHandler()
console_handler.setLevel(logging.DEBUG)
console_format = logging.Formatter("%(asctime)s [%(levelname)s]: %(message)s")
console_handler.setFormatter(console_format)
logger.addHandler(console_handler)


# SCRIPT
def create_prometheus_request(query):
    return requests.get(PROMETHEUS_API_BASE, params = {"query": query})


def is_valid_prometheus_response(response):
    responsestatus = response.json()["status"]
    if responsestatus == "error":
        logger.error("Fetch from Prometheus failed - errorType: [%s], errorMessage: [%s]", response.json()["errorType"], response.json()["error"])
        return False

    if responsestatus != "success":
        logger.error("Fetch from Prometheus failed with unknown error - Response: %s", json.dumps(response.json()))
        return False
    return True


def group_by_market(provider_metrics, running_instances):
    metric_by_market = defaultdict(dict)
    providers = provider_metrics.json()["data"]["result"]
    for provider in providers:
        # Metric by market holds the provider metrics by market.
        # Here we update the market by adding the provider together with the calculated value.
        # The calculated value is the number instances where the provider is circuit broken divided by the total number of running instances.
        metric_by_market[provider["metric"]["market"].lower()].update({provider["metric"]["provider"]: float(provider["value"][1]) / int(running_instances)})
    return metric_by_market


def create_statuspage_request(method, path, payload = None, body = None):
    url = STATUSPAGE_API_BASE + PAGE_ID + path
    if method == "POST" or method == "PUT":
        headers = {"Content-Type": "application/json", "Authorization": "OAuth " + STATUSPAGE_API_KEY}
    else:
        headers = {"Authorization": "OAuth " + STATUSPAGE_API_KEY}
    return create_request(method, url, headers, payload, body)


def create_request(method, url, headers, payload = None, body = None):
    if method == "GET":
        return requests.get(
            url,
            headers = headers,
            params = payload
        )
    if method == "DELETE":
        return requests.delete(
            url,
            headers = headers
        )
    if method == "POST":
        return requests.post(
            url,
            headers = headers,
            data = body
        )
    if method == "PUT":
        return requests.put(
            url,
            headers = headers,
            data = body
        )


def build_update_component_status_request_body(new_status):
    return {
        "component": {
            "status": new_status
        }
    }


def calculate_status(value):
    if value == NOT_CIRCUIT_BROKEN:
        return "operational"

    previouslimit = NOT_CIRCUIT_BROKEN
    for upper_limit, status in STATUS_ENUMS.items():
        if previouslimit < value <= upper_limit:
            return status
        previouslimit = upper_limit


def create_missing_components(names_of_missing_components, group_id, providers_from_prometheus):
    logger.info("Creating missing components: [{}]".format(names_of_missing_components))
    for component_name in names_of_missing_components:
        provider_value = providers_from_prometheus[component_name]
        status = calculate_status(provider_value)
        request_body = build_missing_components_request(component_name, status, group_id)
        r = create_statuspage_request("POST", COMPONENTS_PATH, body = json.dumps(request_body))
        if r.status_code != 201:
            logger.error("Failed to create missing component for component name: [{}]".format(component_name))


def build_missing_components_request(name, status, group_id):
    return {
        "component": {
            "description": "",
            "status": status,
            "name": name,
            "only_show_if_degraded": True,
            "group_id": group_id,
            "showcase": True
        }
    }


def group_by_group_id(all_components):
    components_by_group_id = defaultdict(dict)
    for component in all_components:
        components_by_group_id[component["group_id"]].update({component["name"]: (component["id"], component["status"])})
    return components_by_group_id


def process_component(component_name, component_info, provider_metric_value):
    if provider_metric_value == None:
        logger.warning("Component exists but there are no metrics available - Provider name: [{}]".format(component_name))
        return
    component_id = component_info[0]
    component_status = component_info[1]
    new_status = calculate_status(provider_metric_value)

    # Only update the status if is acctually have changed
    if component_status == new_status:
        return

    logger.info("The status has changed for [%s], updating status [%s] -> [%s]", component_name, component_status, new_status)

    payload = build_update_component_status_request_body(new_status)
    r = create_statuspage_request("PUT", COMPONENTS_PATH + component_id, body = json.dumps(payload))
    if r.status_code == 200:
        logger.info("Successfully updated the status to [%s]", new_status)
    else:
        logger.warning("Status updated of component [%s] failed with statusCode [%s] and message [%s]", component_name, r.status_code, r.json()['error'])


def main():
    if not STATUSPAGE_API_KEY:
        logger.error("Missing api key")
        return 1

    logger.info("Starting cronjob to calculate provider statistics")

    # Get the number of running instances for aggregation production
    # Example: 
    # {
    #   "status": "success",
    #   "data": {
    #       "resultType": "vector",
    #       "result":[
    #           {
    #               "metric": {},
    #               "value":[1544520295.983,"9"]
    #           }
    #       ]
    #   }
    # }
    instances_metric = create_prometheus_request(INSTANCES_QUERY)

    if not is_valid_prometheus_response(instances_metric):
        return 1

    total_running_instances = int(instances_metric.json()["data"]["result"][0]["value"][1])

    if total_running_instances <= 0:
        logger.error("Total running instances was [{}] - Aborting cron job".format(total_running_instances))
        return 1

    # The the circuit breaker metrics by provider
    provider_metrics = create_prometheus_request(PROVIDERS_QUERY)
    if not is_valid_prometheus_response(provider_metrics):
        return 1
    
    # Example: 
    # {
    #   "se": {
    #       "provider-name-1": 0.2, 
    #       "provider-name-2": 0, 
    #       "provider-name-3": 1, 
    #       ...
    #   }
    # }
    provider_metrics_by_market = group_by_market(provider_metrics, total_running_instances)
    
    # Fetch all available components
    # Example: [
    #   {
    #       "id": "string",
    #       "page_id": "string",
    #       "group_id": "string",
    #       "created_at": "2018-12-11T09:26:03Z",
    #       "updated_at": "2018-12-11T09:26:03Z",
    #       "group": true,
    #       "name": "string",
    #       "description": "string",
    #       "position": 0,
    #       "status": "operational",
    #       "showcase": true,
    #       "only_show_if_degraded": true,
    #       "automation_email": "string"
    #   }
    # ]
    all_components = create_statuspage_request("GET", COMPONENTS_PATH)
    if len(all_components.json()) == 0:
        logger.warning("The number of total components was zero")
        return

    # Example:
    # {
    #   "group_id_1": {
    #       "component-1": ("id", "status"),
    #       "component-2": ("id", "status"),
    #       "component-3": ("id", "status"),
    #       ...
    #   }
    # }
    grouped_components = group_by_group_id(all_components.json())

    # Each market is a separate component group
    for market in GROUP_IDS.keys():
        provider_metrics = provider_metrics_by_market[market]
        group_id = GROUP_IDS[market]
        components = grouped_components[group_id]

        available_component_names = set()

        for component_name, component_info in components.items():
            provider_metric_value = provider_metrics.get(component_name, None)
            process_component(component_name, component_info, provider_metric_value)

        # Check if there are any providers in the metrics that don't have an corresponding component
        # OBS! Do not change order of the comparison since it gives what is available in the first set but not the last
        missing_components = set(provider_metrics.keys()).difference(set(components.keys()))
        if missing_components != set():
            create_missing_components(missing_components, group_id, provider_metrics)

    logger.info("Cronjob ran successfully")
    return 0


if __name__ == "__main__":
    sys.exit(main())
