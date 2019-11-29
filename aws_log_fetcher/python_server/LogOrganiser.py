import json
import os


def organise(output_folder):
    # Load metadata
    with open(os.path.join(output_folder, "metadata.json"), "r", encoding="utf-8") as input_file:
        metadata = json.load(input_file)

    # Detect different providers, for each provider create a folder and copy the logs to corresponding folder
    log_file_names_per_provider_name = {}

    for log_metadata in metadata:
        provider_name = log_metadata["providerName"]

        if provider_name not in log_file_names_per_provider_name:
            log_file_names_per_provider_name[provider_name] = []

        log_file_names_per_provider_name[provider_name].append(log_metadata["unique_file_name"])

    # For each provider folder, keep the list of paths of corresponding logs
    log_paths_per_provider_folder = {}

    # For each provider, we will create a folder and we will move each log into its
    # corresponding folder
    for provider_name in log_file_names_per_provider_name:
        provider_folder = os.path.join(output_folder, provider_name)
        os.mkdir(provider_folder)
        log_paths_per_provider_folder[provider_folder] = []

        # For each log file, move it to the corresponding provider folder
        for file_name in log_file_names_per_provider_name[provider_name]:
            old_file_path = os.path.join(output_folder, file_name)
            new_file_path = os.path.join(output_folder, provider_name, file_name)
            if os.path.exists(old_file_path):
                os.rename(old_file_path, new_file_path)
                log_paths_per_provider_folder[provider_folder].append(new_file_path)

    # In each provider folder, we will split the logs futher by considering the endpoint
    # they end. We will collect the logs that end in the same endpoint in the same folder
    for provider_folder in log_paths_per_provider_folder:
        provider_logs_per_endpoint = {}

        for log_file in log_paths_per_provider_folder[provider_folder]:
            with open(log_file, "r", encoding="utf-8") as input_file:
                log_data = input_file.read()

            # We will find in which endpoint the log ended...

            # First step: Find out how many request/response pair are there
            # and then grasp the last request/response pair...
            index = 0
            while True:
                if str(index + 1) + " * Client out-bound request" in log_data:
                    index += 1
                else:
                    break

            last_request_response_beginning_line = str(index) + " * Client out-bound request"

            last_request_response_data = log_data[
                                         log_data.index(last_request_response_beginning_line):]

            last_endpoint = [x for x in last_request_response_data.split("\n") if " GET " in x or " POST " in x][0]

            if "POST" in last_endpoint:
                last_endpoint = last_endpoint.split("POST")[1].strip()
            else:
                last_endpoint = last_endpoint.split("GET")[1].strip()

            # TODO: Grouping by "last endpoint" does not help really as each endpoint can take different
            # query parameters or different path value (for example for transaction fetching the endpoints
            # generally like .../<accountId>/transactions?queryParam1=value1&...
            # For such cases, two logs will have different last endpoints even though they end in the same
            # operation. We need a more clever way of grouping the logs
            if last_endpoint not in provider_logs_per_endpoint:
                provider_logs_per_endpoint[last_endpoint] = []

            provider_logs_per_endpoint[last_endpoint].append(log_file)

        for index, last_endpoint in enumerate(provider_logs_per_endpoint):
            os.makedirs(os.path.join(provider_folder, str(index)))
            log_files = provider_logs_per_endpoint[last_endpoint]
            for log_file in log_files:
                file_name = log_file.split(os.sep)[-1]
                destination_file = os.path.join(provider_folder, str(index), file_name)
                os.rename(log_file, destination_file)
