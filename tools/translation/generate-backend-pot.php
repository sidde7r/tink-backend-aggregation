#!/usr/bin/php
<?php
require_once('oneskyclient.php');
require_once('potfilegenerator.php');

$oneSkyProjectId = 154300;
$oneSkyClient = new OneSkyClient();
$potFileGenerator = new PotFileGenerator();

// Add the strings to the tink-backend.pot file
$generatedFilename = $potFileGenerator->generateBackendPotFile();

// Use xgettext to remove duplicate strings from the .pot file
echo "Removing duplicate strings from '" . $generatedFilename . "'..." . PHP_EOL;
exec('msguniq --use-first --no-location ../../po/' . $generatedFilename . ' -o ../../po/' . $generatedFilename);

// Upload the generated tink-backend.pot file to OneSky
$oneSkyClient->upload($generatedFilename);
?>
