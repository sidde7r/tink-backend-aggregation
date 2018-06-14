#!/usr/bin/php
<?php
require_once('oneskyclient.php');
require_once('potfilegenerator.php');

$oneSkyClient = new OneSkyClient();
$potFileGenerator = new PotFileGenerator();

// Add the strings to the tink-backend.pot file
$generatedFilename = $potFileGenerator->generateBackendPotFile();
// Upload the generated tink-backend.pot file to OneSky
$oneSkyClient->upload($generatedFilename);
?>
