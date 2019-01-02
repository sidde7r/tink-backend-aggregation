<?php
require_once('onesky.php');

class OneSkyClient {
    private $apiKey = "E8omlD3G9COIQJQFAsFivgtQuoqmgEsK";
    private $apiSecret = "oMUiSmpQvFIiJo4iY1M9RdLkuo5K11ng";
    private $baseLocale = "en_US";
    private $client;

    public function __construct(array $arguments = array()) {
        $this->client = new Onesky_Api();
        $this->client->setApiKey($this->apiKey);
        $this->client->setSecret($this->apiSecret);
    }

    public function upload($filename, $projectId) {

        echo "Uploading '" . $filename . "'..." . PHP_EOL;
        $response = $this->client->files('upload', array(
            'project_id'                => $projectId,
            'file'                      => '../../po/' . $filename,
            'file_format'               => 'GNU_PO',
            'locale'                    => $this->baseLocale,
            'is_keeping_all_strings'    => FALSE
        ));

        echo "Upload response:" . PHP_EOL;
        echo json_encode(json_decode($response, true), JSON_PRETTY_PRINT) . PHP_EOL;
    }
}
?>
