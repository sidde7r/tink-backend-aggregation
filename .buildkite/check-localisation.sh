#!/bin/bash
# Checks if all localisable strings are added to the POT file.

set -e
#Colors
RED='\033[0;31m'
NOCOLOR='\033[0m'

#list all java srcs
#Get all localisable keys from these files, excluding those already po/tink-aggr-backend.pot
newPotContent=$(find src/ -name *.java ! -name *Test.java\
  -exec xgettext --from-code="UTF-8" -kgetString -kgetPluralString:1,2 -kLocalizableKey\
  -kLocalizableParametrizedKey -kLocalizablePluralKey:1,2 -x po/tink-aggr-backend.pot --omit-header -o -  {} +)

if [ -z "$newPotContent" ]
then
  echo "All localisable strings are in the POT files."
else
  echo -e "${RED}"
  echo "-->"
  echo "--> The following localisable strings are missing in the POT file:"
  echo "$newPotContent"
  echo "--> Execute the following PHP command:"
  echo "-->   php tools/translation/generate-backend-pot.php"
  echo "-->"
  echo -e "${NOCOLOR}"
  exit 1
fi
