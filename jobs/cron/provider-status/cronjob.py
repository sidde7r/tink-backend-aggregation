#!/usr/bin/env python3

import requests
import json
import logging
import sys
import os
import re
import time
from collections import defaultdict

# STATUS PAGE
STATUSPAGE_API_KEY = os.environ.get("STATUSPAGE_API_KEY").rstrip("\n")
STATUSPAGE_API_BASE = "https://api.statuspage.io/v1/pages/"
PAGE_ID = "x1lbt12g0ryw"  # https://tink-enterprise.statuspage.io/
COMPONENTS_PATH = "/components/"
BANK_UNAVAILABLE_IDENTIFIER = "(bank status)"
# Keep first space - it's not in Prometheus provider name so must be replaced
BANK_UNAVAILABLE_REGEX = r"\ \(bank status\)$"
STATUS_ENUMS = {
    0.50: "degraded_performance",
    0.75: "partial_outage",
    1: "major_outage"
}

GROUP_IDS = {'se': 'c20kyrkjrgks', 'be': 'lw36806hwvfm', 'at': 'n8p3fc6ltzfw',
             'es': 'zxf8mcfwz08q', 'fi': 'rkknf5992fpg', 'uk': 'cbvwtp1bxzhm',
             'dk': 'f1bctj61c0bv', 'no': 't99h0j1xfrj0', 'de': 'zflg1lrtvhpp',
             'pt': '5d71y2xjh9fm', 'nl': 'jw9m70dk8379', 'it': '5f9pbpx5bzzg',
             'fr': '6hy84x3bjnwc'}

# PROMETHEUS
PROMETHEUS_API_BASE = "http://prometheus.monitoring-prometheus.svc.cluster.local:9090/api/v1/query"

# Queries
PROVIDERS_QUERY = "sum(increase(tink_agent_login_total{action='login',provider!~'.*abstract.*',className!~'abnamro.*|demo.DemoAgent|nxgen.demo.*|fraud.CreditSafeAgent'}[30m])) by (provider, market)"
FAILING_PROVIDERS_QUERY = "sum(increase(tink_agent_login_total{action='login', outcome='failed',provider!~'.*abstract.*',className!~'abnamro.*|demo.DemoAgent|nxgen.demo.*|fraud.CreditSafeAgent'}[30m])) by (provider, market)/sum(increase(tink_agent_login_total{action='login',provider!~'.*abstract.*',className!~'abnamro.*|demo.DemoAgent|nxgen.demo.*|fraud.CreditSafeAgent'}[30m])) by (provider, market)"
UNAVAILABLE_PROVIDERS_QUERY = "sum(increase(tink_agent_login_total{action='login', outcome='unavailable',provider!~'.*abstract.*',className!~'abnamro.*|demo.DemoAgent|nxgen.demo.*|fraud.CreditSafeAgent'}[30m])) by (provider, market)/sum(increase(tink_agent_login_total{action='login',provider!~'.*abstract.*',className!~'abnamro.*|demo.DemoAgent|nxgen.demo.*|fraud.CreditSafeAgent'}[30m])) by (provider, market)"

# LOGGING
logging.basicConfig(stream=sys.stdout, level=logging.INFO)
logger = logging.getLogger('statuspage_provider_status')


# SCRIPT
def create_prometheus_request(query):
    response = requests.get(PROMETHEUS_API_BASE,
                            params={"query": query},
                            headers={
                                'Content-Type': 'application/json; charset=utf-8',
                            })
    return response


def is_valid_prometheus_response(response):
    responsestatus = response.json()["status"]
    if responsestatus == "error":
        logger.error("Fetch from Prometheus failed - errorType: [%s], errorMessage: [%s]",
                     response.json()["errorType"],
                     response.json()["error"])
        return False

    if responsestatus != "success":
        logger.error("Fetch from Prometheus failed with unknown error - Response: %s", json.dumps(response.json()))
        return False
    return True


def group_by_market(providers):
    metric_by_market = defaultdict(dict)
    for provider in providers:
        # Metric by market holds the provider metrics by market.
        # Here we update the market by adding the provider together with the calculated value.
        # The calculated value is the number instances where the provider is circuit
        # broken divided by the total number of running instances.
        market_name = provider["metric"]["market"].lower()
        provider_name = provider["metric"]["provider"]
        metric_value = float(provider["value"][1])
        metric_by_market[market_name].update({provider_name: metric_value})
    return metric_by_market


def create_statuspage_request(method, path, payload=None, body=None):
    url = STATUSPAGE_API_BASE + PAGE_ID + path
    if method == "POST" or method == "PUT":
        headers = {"Content-Type": "application/json", "Authorization": "OAuth " + STATUSPAGE_API_KEY}
    else:
        headers = {"Authorization": "OAuth " + STATUSPAGE_API_KEY}
    return create_request(method, url, headers, payload, body)


def create_request(method, url, headers, payload=None, body=None):
    if method == "GET":
        return requests.get(
            url,
            headers=headers,
            params=payload
        )
    if method == "DELETE":
        return requests.delete(
            url,
            headers=headers
        )
    if method == "POST":
        return requests.post(
            url,
            headers=headers,
            data=body
        )
    if method == "PUT":
        return requests.put(
            url,
            headers=headers,
            data=body
        )


def build_update_component_status_request_body(new_status):
    return {
        "component": {
            "status": new_status
        }
    }


def calculate_status(value):
    # TODO restore previous more readable structure
    status = "operational"

    previouslimit = 0
    for upper_limit, s in STATUS_ENUMS.items():
        if previouslimit < value <= upper_limit:
            status = s
        previouslimit = upper_limit
    return status


def create_missing_components(names_of_missing_components, group_id):
    logger.info("Creating missing components: [{}]".format(names_of_missing_components))
    failures = 0
    for component_name in names_of_missing_components:
        provider_value = 0
        status = calculate_status(provider_value)
        request_body = build_missing_components_request(component_name, status, group_id, False)
        r = create_statuspage_request("POST", COMPONENTS_PATH, body=json.dumps(request_body))
        if r.status_code == 422:
            logger.warning("Failed to create missing component for component name: [{}]".format(component_name))
            failures += 1
            # With more than 5 failures for one run we're probably rate-limited, keep updating components on next run
            if failures > 5:
                return 422
        elif r.status_code != 201:
            logger.error("Failed to create missing component for component name: [{}], error code was {}".format(
                component_name, r.status_code))
            return 1
        request_body = build_missing_components_request(component_name + " " + BANK_UNAVAILABLE_IDENTIFIER,
                                                        status, group_id, True)
        r = create_statuspage_request("POST", COMPONENTS_PATH, body=json.dumps(request_body))
        if r.status_code == 422:
            logger.warning("Failed to create missing component for component name: [{}]".format(component_name))
            failures += 1
            # With more than 5 failures for one run we're probably rate-limited, keep updating components on next run
            if failures > 5:
                return 422
        elif r.status_code != 201:
            logger.error("Failed to create missing component for component name: [{}], error code was {}".format(
                component_name, r.status_code))
            return 1
    return 0


def build_missing_components_request(name, status, group_id, hide):
    return {
        "component": {
            "description": "",
            "status": status,
            "name": name,
            "only_show_if_degraded": hide,
            "group_id": group_id,
            "showcase": True
        }
    }


def group_by_group_id(all_components):
    components_by_group_id = defaultdict(dict)
    for component in all_components:
        components_by_group_id[component["group_id"]].update(
            {component["name"]: (component["id"], component["status"])}
        )
    return components_by_group_id


def process_component(component_name, component_info, provider_metric_value):
    if provider_metric_value is None:
        # Nothing has happened so nothing to update
        return
    component_id = component_info[0]
    component_status = component_info[1]
    new_status = calculate_status(provider_metric_value)

    # Only update the status if is acctually have changed
    if component_status == new_status:
        return

    logger.info("The status has changed for [%s], updating status [%s] -> [%s]",
                component_name, component_status, new_status)

    payload = build_update_component_status_request_body(new_status)
    r = create_statuspage_request("PUT", COMPONENTS_PATH + component_id, body=json.dumps(payload))
    if r.status_code == 200:
        logger.info("Successfully updated the status to [%s]", new_status)
    else:
        logger.warning("Status updated of component [%s] failed with statusCode [%s] and message [%s]",
                       component_name, r.status_code, r.json()['error'])


def main():
    if not STATUSPAGE_API_KEY:
        logger.error("Missing api key")
        return 1

    # We have seen that the job fails on the first try many times.
    # The theory is that the job starts faster than the outgoing
    # connections are opened. Hence we've added this sleep to
    # ensure that everything get the time to start up.
    time.sleep(30)

    logger.info("Starting cronjob to calculate provider statistics")

    # The total number of logins per provider over the last N minutes
    all_logins_response = create_prometheus_request(PROVIDERS_QUERY)
    if not is_valid_prometheus_response(all_logins_response):
        return 1

    # The number of those logins that failed - approximation for 'error is with us'
    failed_logins_response = create_prometheus_request(FAILING_PROVIDERS_QUERY)
    if not is_valid_prometheus_response(failed_logins_response):
        return 1

    # The number of logins with outcome 'unavailable' - approximation for 'error is with bank'
    unavailable_logins_response = create_prometheus_request(UNAVAILABLE_PROVIDERS_QUERY)
    if not is_valid_prometheus_response(failed_logins_response):
        return 1

    # Restructure Prometheus responses so we can work with them
    #
    # Example:
    # {
    #   "se": {
    #       "provider-name-1": 0.2,
    #       "provider-name-2": 0,
    #       "provider-name-3": 1,
    #       ...
    #   }
    # }
    #
    all_logins_by_market = group_by_market(all_logins_response.json()["data"]["result"])
    failed_logins_by_market = group_by_market(failed_logins_response.json()["data"]["result"])
    unavailable_logins_by_market = group_by_market(unavailable_logins_response.json()["data"]["result"])

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
    all_components = create_statuspage_request("GET", COMPONENTS_PATH).json()
    if len(all_components) == 0:
        logger.error("The number of total components was zero")
        return 1

    # Example:
    # {
    #   "group_id_1": {
    #       "component-1": ("id", "status"),
    #       "component-2": ("id", "status"),
    #       "component-3": ("id", "status"),
    #       ...
    #   }
    # }
    grouped_components = group_by_group_id(all_components)

    # Each market is a separate component group
    for market in GROUP_IDS.keys():
        provider_logins = all_logins_by_market[market]
        provider_failed_logins = failed_logins_by_market[market]
        provider_unavailable_logins = unavailable_logins_by_market[market]
        group_id = GROUP_IDS[market]
        components = grouped_components[group_id]

        for component_name, component_info in components.items():
            provider_metric_value = None
            # The "bank status" suffix is used by us on Statuspage to indicate that errors aren't with Tink, but an
            # integration still isn't in a usable state. In Prometheus this is reflected as two different 'outcomes' on
            # the agent_login time series - 'failed' for Tink problems, 'unavailable' for bank problems. Since both are
            # on the same time series they use the same key and the suffix has to be removed before looking up values.
            if re.search(BANK_UNAVAILABLE_REGEX, component_name):
                provider_name = re.sub(BANK_UNAVAILABLE_REGEX, "", component_name)
                logins = provider_logins.get(provider_name, None)  # Remove suffix
                if logins is not None and logins > 0.0:  # Only look for failed logins if there have been logins
                    provider_metric_value = provider_unavailable_logins.get(provider_name, 0)
                process_component(component_name, component_info, provider_metric_value)
            else:
                logins = provider_logins.get(component_name, None)
                # If there haven't been any logins at all, status won't be updated
                if logins is not None and logins > 0.0:
                    provider_metric_value = provider_failed_logins.get(component_name, 0)
                    # ...but the component is still processed, so we get something in the logs?
                process_component(component_name, component_info, provider_metric_value)

        # Check if there are any providers in the metrics that don't have an corresponding component
        # OBS! Do not change order of the comparison since it gives what is available in the first set but not the last
        # missing_components = set(provider_logins.keys()).difference(set(components.keys()))
        # if missing_components != set():
        #     if create_missing_components(missing_components, group_id) == 422:
        #         logger.info("Rate limited by Statuspage, waiting...")
        #         time.sleep(5)

    logger.info("Cronjob ran successfully")
    return 0


if __name__ == "__main__":
    sys.exit(main())
