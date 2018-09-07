#!/usr/bin/php
<?php
require_once('oneskyclient.php');
require_once('potfilegenerator.php');

// Init the client.
$oneSkyClient = new OneSkyClient();
$potFileGenerator = new PotFileGenerator();

// Add the strings to the tink-provider.pot file
$generatedFilename = $potFileGenerator->generateProviderPotFile();

// Use xgettext to remove duplicate strings from the .pot file
echo "Removing duplicate strings from '" . $generatedFilename . "'..." . PHP_EOL;
exec('msguniq --use-first --no-location ../../po/' . $generatedFilename . ' -o ../../po/' . $generatedFilename);

// Upload the generated tink-provider.pot file to OneSky
// TODO: Uncomment below code to enable uploading the generated .pot file to OneSky
// TODO: Don't enable uploading to OneSky until all translatable provider fields are in English
// TODO: Until that's done run this script for one market/language at the time, and inspect the pot file before uploading
// TDOO: Modify line 133 in the potfilegenerator.php script change which provider configurations are included
/*
$oneSkyClient->upload($generatedFilename);
*/
?>
