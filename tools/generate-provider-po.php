#!/usr/bin/php
<?php
require_once('onesky/oneskyclient.php');
require_once('onesky/potfilegenerator.php');

// Init the client.
$oneSkyClient = new OneSkyClient();
$potFileGenerator = new PotFileGenerator();

// Add the strings to the tink-provider.pot file
$generatedFilename = $potFileGenerator->generateProviderPotFile();
// Upload the generated tink-provider.pot file to OneSky
// TODO: Uncomment below code to enable uploading the generated .pot file to OneSky
// TODO: Don't enable uploading to OneSky until all translatable provider fields are in English
/*
$oneSkyClient->upload($generatedFilename);
*/
?>
