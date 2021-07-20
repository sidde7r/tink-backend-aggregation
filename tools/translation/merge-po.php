#!/usr/bin/php
<?php

require_once('onesky.php');

// Parameters.

$apiKey="E8omlD3G9COIQJQFAsFivgtQuoqmgEsK";
$apiSecret="oMUiSmpQvFIiJo4iY1M9RdLkuo5K11ng";
$projectId = "154300";
$poDir = dirname(__FILE__, 3) . '/po';
$locales = array("da", "de", "es", "fi", "fr", "it", "nl", "no", "pl", "pt", "sv");

// Init the client.
$client = new Onesky_Api();
$client->setApiKey($apiKey)->setSecret($apiSecret);

// Fetch the translations.
foreach ($locales as $locale) {
    print('Downloading \''.$locale.'\'...'.PHP_EOL);

    $response = $client->translations('export', array(
        'project_id'  => $projectId,
        'source_file_name'        => 'tink-aggr-backend.pot',
        'locale' => $locale
    ));

    file_put_contents($poDir.'/'.$locale.'.po', $response);

    exec('msguniq --use-first --no-location ' . $poDir.'/'.$locale.'.po' . ' -o ' .$poDir.'/'.$locale.'.po');
}

?>
