#!/usr/bin/php
<?php
require_once('oneskyclient.php');
require_once('potfilegenerator.php');

$tinkBackendAggrRoot = dirname(__FILE__, 3);

$oneSkyProjectId = 154300;
$oneSkyClient = new OneSkyClient();
$potFileGenerator = new PotFileGenerator();

// Add the strings to the tink-backend.pot file
$generatedFilename = $potFileGenerator->generateBackendPotFile($tinkBackendAggrRoot);
$poAbsolutePath = $tinkBackendAggrRoot . '/po/' . $generatedFilename;

// Use xgettext to remove duplicate strings from the .pot file
echo "Removing duplicate strings from '" . $generatedFilename . "'..." . PHP_EOL;
exec('msguniq --use-first --no-location ' . $poAbsolutePath . ' -o ' . $poAbsolutePath);

// Upload the generated tink-backend.pot file to OneSky
$oneSkyClient->upload($poAbsolutePath, $oneSkyProjectId);
?>
