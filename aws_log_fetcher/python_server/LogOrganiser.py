import json
import os


def organise(output_folder):
    # Load metadata
    with open(os.path.join(output_folder, "metadata.json"), "r", encoding="utf-8") as inp:
        metadata = json.load(inp)

    # Detect different providers, for each provider create a folder and copy the logs to corresponding folder
    provider_issues = {}

    for m in metadata:
        request_id = m["requestId"]
        provider_name = m["providerName"]
        if provider_name not in provider_issues:
            provider_issues[provider_name] = []
        provider_issues[provider_name].append(request_id + "_" + provider_name)

    folders = {}

    for provider in provider_issues:
        provider_folder = os.path.join(output_folder, provider)
        os.mkdir(provider_folder)
        folders[provider_folder] = []
        for file_name in provider_issues[provider]:
            old_file = os.path.join(output_folder, file_name + ".log")
            new_file = os.path.join(output_folder, provider, file_name + ".log")
            if os.path.exists(old_file):
                os.rename(old_file, new_file)
                folders[provider_folder].append(new_file)

    for folder in folders:
        files = folders[folder]
        issues = {}
        for file in files:
            with open(file, "r", encoding="utf-8") as inp:
                data = inp.read()
            temp = "<num> * Client out-bound request"
            index = 1
            while True:
                s = temp.replace("<num>", str(index))
                if s in data:
                    index += 1
                else:
                    break
            last = temp.replace("<num>", str(index - 1))
            data = data[data.index(last):]
            last_endpoint = [x for x in data.split("\n") if " GET " in x or " POST " in x][0]
            if "POST" in last_endpoint:
                last_endpoint = last_endpoint.split("POST")[1].strip()
            else:
                last_endpoint = last_endpoint.split("GET")[1].strip()
            if last_endpoint not in issues:
                issues[last_endpoint] = []
            issues[last_endpoint].append(file)

        for index, issue in enumerate(issues):
            os.makedirs(folder + "/" + str(index))
            source_files = issues[issue]
            for sourceFile in source_files:
                file_name = sourceFile.split(os.sep)[-1]
                destination_file = folder + "/" + str(index) + "/" + file_name
                os.rename(sourceFile, destination_file)
