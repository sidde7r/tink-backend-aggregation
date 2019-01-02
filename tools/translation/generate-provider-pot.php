#!/usr/bin/php
<?php
require_once('oneskyclient.php');
require_once('potfilegenerator.php');

$oneSkyProjectId = 154301;
// Init the client.
$oneSkyClient = new OneSkyClient();
$potFileGenerator = new PotFileGenerator();

// Add the strings to the tink-provider.pot file
$generatedFilename = $potFileGenerator->generateProviderPotFile();

// Use xgettext to remove duplicate strings from the .pot file
echo "Removing duplicate strings from '" . $generatedFilename . "'..." . PHP_EOL;
exec('msguniq --use-first ../../po/' . $generatedFilename . ' -o ../../po/' . $generatedFilename);

// Upload the generated tink-provider.pot file to OneSky
$oneSkyClient->upload($generatedFilename, $oneSkyProjectId);
?>
