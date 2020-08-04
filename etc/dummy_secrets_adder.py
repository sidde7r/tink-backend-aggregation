import glob
import json
import yaml

# Read all production secrets from JSON files that are stored in the aggregation repository
files = glob.glob("data/secret/production/ss/**/*.json", recursive=True)

# After reading each JSON file, we will keep secret data here for each financial institution
transformed_secrets = {}

# Read what dummy values to use for each secret
with open("etc/dummy_values_dict.json", "r", encoding="utf-8") as dummy_values_dict_file:
    dummy_values_for_secret_fields = json.load(dummy_values_dict_file)

print("Reading secrets")

# Parse each JSON file
for file in files:
    try:
        with open(file, "r", encoding="utf-8") as json_file:
            secret_data = json.load(json_file)
    except Exception as err:
        print("Error while reading the JSON file " + file)
        print(err)
        exit(-1)

    # There are two formats for the JSON files that contains secrets: list and dict
    # we should check in which format the file is and parse accordingly
    try:
        if isinstance(secret_data, list):
            if len(secret_data) > 1:
                raise Exception(
                    "The secret file " + file + " contains a list with more than one element.  + "
                                                "This cannot be handled by the script")
            secret_data = secret_data[0]
            # For some financial institutions we don't have "sensitive" section, if this
            # is the case, just add an empty one to prevent getting an exception below
            if "sensitive" not in secret_data:
                secret_data["sensitive"] = {}

            for fin_id in secret_data["finId"]:
                # Merge fields in "secrets" section and "sensitive" section into one section
                transformed_secret = {**secret_data["secrets"], **secret_data["sensitive"]}
                transformed_secret["redirectUrls"] = dummy_values_for_secret_fields["redirectUrl"]
                transformed_secrets[fin_id] = transformed_secret
        elif isinstance(secret_data, dict):
            # In this format, each key must correspond with a fin_id
            for fin_id in secret_data.keys():
                # For some financial institutions we don't have "sensitive" section, if this
                # is the case, just add an empty one to prevent getting an exception below
                if "sensitive" not in secret_data[fin_id]:
                    secret_data[fin_id]["sensitive"] = {}
                # Merge fields in "secrets" section and "sensitive" section into one section
                transformed_secret = {**secret_data[fin_id]["secrets"], **secret_data[fin_id]["sensitive"]}
                transformed_secret["redirectUrls"] = dummy_values_for_secret_fields["redirectUrl"]
                transformed_secrets[fin_id] = transformed_secret
        else:
            raise Exception("The secret file " + file + " is in unknown format that could not be parsed")
    except Exception as err:
        print("Error while parsing the JSON file " + file)
        print(err)
        exit(-1)

print("Replacing secrets with dummy values")

for fin_id in transformed_secrets:
    # Since we need to have "redirectUrl: ..." instead of "redirectUrls: [...]" in YML file
    # we will perform the necessary transformation
    del transformed_secrets[fin_id]["redirectUrls"]
    transformed_secrets[fin_id]["redirectUrl"] = dummy_values_for_secret_fields["redirectUrl"]

    # Replace real secret values with dummy values
    for key in transformed_secrets[fin_id]:
        if key not in dummy_values_for_secret_fields:
            raise Exception("For finId = " + fin_id + " a dummy value for field = " + key + " is not set. " +
                            "Please add a dummy value for this field in dummy_values_dict.json")
        transformed_secrets[fin_id][key] = dummy_values_for_secret_fields[key]

print("Modifying test.yml file")

# Read test.yml file
with open("etc/test.yml", "r", encoding="utf-8") as yml_file:
    parsed_yaml_file = yaml.full_load(yml_file)

secrets_in_yml_file = parsed_yaml_file["agentsServiceConfiguration"]["integrations"]
for fin_id in transformed_secrets:
    if fin_id not in secrets_in_yml_file:
        secrets_in_yml_file[fin_id] = {}
    secrets_in_yml_file[fin_id]["tink"] = transformed_secrets[fin_id]

with open("etc/test.yml", "w", encoding="utf-8") as yml_file:
    yaml.dump(parsed_yaml_file, yml_file, default_flow_style=False, sort_keys=True)

print("Dummy secrets are dumped to test.yml file")
