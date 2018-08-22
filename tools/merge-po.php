#!/usr/bin/php
<?php

require_once('lib/onesky.php');

// Parameters.

$apiKey="E8omlD3G9COIQJQFAsFivgtQuoqmgEsK";
$apiSecret="oMUiSmpQvFIiJo4iY1M9RdLkuo5K11ng";
$projectId = "11489";
$locales = $arr = array("sv", "fr", "en_GB", "nl", "da", "no", "fi", "de");

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

	$providerResponse = $client->translations('export', array(
        'project_id'  => $projectId,
        'source_file_name'        => 'tink-provider.pot',
        'locale' => $locale
    ));

    $providerTranslations = preg_replace("~^(.*\n){12}\n~", "", $providerResponse);

    file_put_contents('po/'.$locale.'.po', $response . $providerTranslations);
    exec('msguniq --use-first --no-location po/' .$locale.'.po' . ' -o po/' .$locale.'.po');
}

print('Converting translations...');
exec('cd src/common-lib && mvn gettext:dist && cd ../../');

?>
