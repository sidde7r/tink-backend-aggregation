import re
import json, sys
from os import listdir, path

SEEDING_DIR = "../data/seeding"

base_provider = None
for filename in listdir(SEEDING_DIR):
    if re.compile("^providers\\-..\\.json").match(filename):
        baseproviderfilename = path.join(SEEDING_DIR, filename)

        with open(baseproviderfilename) as f:
            data = json.load(f)
            providers = data.get('providers')
            for provider in providers:
                if 'financialInstitutionId' not in provider:
                    provider.update({'financialInstitutionId': 'dummyId'})
                    provider.update({'financialInstitutionName': 'dummyName'})

        with open(baseproviderfilename, 'w') as f:
            f.write(json.dumps(data, sort_keys=True, indent=4, separators=(',', ': '), ensure_ascii=False))
            f.write("\n")
        f.close()
