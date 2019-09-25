#!/usr/bin/env python3

import requests, json, logging, sys, os


# Config
PAGE_ID = "d6080gsy9zbj"
MARKETS = ['se', 'be', 'at', 'es', 'fi', 'uk', 'dk', 'no', 'de', 'pt', 'nl']
PLACEHOLDER_DESCRIPTION = "This component is created so that the group is belongs to isn't automatically deleted by " \
                          "Statuspage. Please ignore."

# STATUS PAGE
STATUSPAGE_API_KEY = os.environ.get("STATUSPAGE_API_KEY").rstrip("\n")
STATUSPAGE_API_BASE = "https://api.statuspage.io/v1/pages/"

# LOGGING
logging.basicConfig(stream=sys.stdout, level=logging.INFO)
logger = logging.getLogger('statuspage_provider_status_helper_markets')

def create_statuspage_request(method, path, payload=None, body=None):
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
    else:
        logger.error("No request created for {}, unknown request type {}".format(url, method))


def build_create_components_request(name, description, status, hide=True, showcase=False):
    return json.dumps({
        "component": {
            "description": description,
            "status": status,
            "name": name,
            "only_show_if_degraded": hide,
            "showcase": showcase
        }
    })


def build_create_component_group_request(name, component_id):
    return json.dumps({
        "component_group": {
            "components": [component_id],
            "name": name,
        }
    })


groups = {}

# Add all integration groups already on statuspage
all_groups = create_statuspage_request("GET", "/component-groups/").json()
for group in all_groups:
    if group["name"][-12:] == "integrations":
        groups[group["name"][0:2].lower()] = group["id"]


for market in MARKETS:
    # Create new market groups if needed
    if market not in groups.keys():
        placeholder_response = create_statuspage_request(
            "POST",
            "/components",
            body=build_create_components_request(
                "{} integrations group placeholder".format(market.upper()),
                PLACEHOLDER_DESCRIPTION,
                "operational")
        )
        if placeholder_response.status_code != 201:
            logger.error("Failed to create placeholder component for market: [{}]".format(market))
            continue
        logger.info("Created placeholder component for market: [{}]".format(market))
        placeholder_id = placeholder_response.json()["id"]
        group_response = create_statuspage_request(
            "POST",
            "/component-groups/",
            body = build_create_component_group_request( "{} integrations".format(market.upper()), placeholder_id)
        )
        if group_response.status_code != 201:
            logger.error("Failed to create component group for market: [{}]".format(market))
            continue
        logger.info("Created component group for market: [{}]".format(market))

        groups[market] = group_response.json()["id"]

# Print created groups to stdout for copy-pasteing in cronjob.py
print(groups)
