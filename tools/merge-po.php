#!/usr/bin/php
<?php

require_once('translation/onesky.php');

// Parameters.

$apiKey="E8omlD3G9COIQJQFAsFivgtQuoqmgEsK";
$apiSecret="oMUiSmpQvFIiJo4iY1M9RdLkuo5K11ng";
$projectId = "11489";
$locales = $arr = array("sv", "fr","en_GB","nl");

// Init the client.

$client = new Onesky_Api();
$client->setApiKey($apiKey)->setSecret($apiSecret);

// Fetch the translations.

foreach ($locales as $locale) {
	print('Downloading \''.$locale.'\'...'.PHP_EOL);

	$response = $client->translations('export', array(
	    'project_id'  => $projectId,
	    'source_file_name'        => 'tink-backend.pot',
	    'locale' => $locale
	));

	file_put_contents('po/'.$locale.'.po', $response);
}

print('Converting translations...');
exec('cd src/common-lib && mvn gettext:dist && cd ../../');

?>
