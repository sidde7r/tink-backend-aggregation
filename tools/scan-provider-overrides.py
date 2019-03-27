import re
import json, sys
from os import listdir, path
import difflib

SEEDING_DIR = "data/seeding"
OVERRIDES_BASE = path.join(SEEDING_DIR, "providers/overriding-providers")

providername = sys.argv[1]
print('Scanning overrides for provider "' + providername + "'")

baseProvider = None
for filename in listdir(SEEDING_DIR):
    if re.compile("^providers\\-..\\.json").match(filename):
        baseproviderfilename = path.join(SEEDING_DIR, filename)

        with open(baseproviderfilename) as baseproviderfile:
            data = json.load(baseproviderfile)

            if 'providers' not in data:
                print("WARN: providers not found in " + baseproviderfilename)
            else:

                for provider in data['providers']:
                    if provider['name'] == providername:
                        if baseProvider is not None:
                            print("WARN: Duplicate provider found, ignoring. Found in file: " + baseproviderfilename)
                        else:
                            print("Found provider in " + baseproviderfilename)
                            baseProvider = provider

firstOverride = None

for clusterdir in listdir(OVERRIDES_BASE):
    providerFound = False

    for overridefilename in listdir(path.join(OVERRIDES_BASE, clusterdir)):

        overridefilepath = path.join(OVERRIDES_BASE, clusterdir, overridefilename)

        with open(overridefilepath) as overridefile:
            data = json.load(overridefile)

            if 'provider-configuration' not in data:
                print("WARN: overrides not present in " + overridefilepath)
            else:
                for override in data['provider-configuration']:

                    if override['name'] == providername:
                        if providerFound:
                            print("WARN: provider overridden more than once in " + clusterdir)
                        else:
                            providerFound = True
                            print("Found override in " + overridefilepath)

                        if firstOverride is None:
                            firstOverride = override

                            origjson = json.dumps(baseProvider, indent=4, sort_keys=True, default=str).split("\n")
                            overjson = json.dumps(override, indent=4, sort_keys=True, default=str).split("\n")

                            for line in difflib.unified_diff(origjson, overjson):
                                print(line)
                        else:
                            print("Diffing to first override")

                            origjson = json.dumps(firstOverride, indent=4, sort_keys=True, default=str).split("\n")
                            overjson = json.dumps(override, indent=4, sort_keys=True, default=str).split("\n")
                            for line in difflib.unified_diff(origjson, overjson):
                                print(line)
