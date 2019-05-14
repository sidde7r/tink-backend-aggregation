<?php
class PotFileGenerator {
    private $backendPotFilename = "tink-aggr-backend.pot";
    private $providerPotFilename = "tink-aggr-provider.pot";

    private $passwordHelpTextKey = "passwordHelpText";
    private $nameKey = "name";
    private $displayDescriptionKey = "displayDescription";
    private $fieldsKey = "fields";
    private $hintKey = "hint";
    private $helpTextKey = "helpText";
    private $descriptionKey = "description";
    private $patternErrorKey = "patternError";
    private $supplementalFieldsKey = "supplementalFields";

    public function generateBackendPotFile() {
        echo "Generating '" . $this->backendPotFilename . "'..." . PHP_EOL;
        exec ('find ../../src/ -name \'*.java\' ! -path \'*Lookup*\' ! -name SeedMerchantsCommand.java ! -name \'*Test.java\' -exec xgettext --from-code="UTF-8" -kgetString -kgetPluralString:1,2 -kLocalizableKey -kLocalizableParametrizedKey -kLocalizablePluralKey:1,2 --join-existing -o ../../po/' . $this->backendPotFilename . ' {} +');
        echo "Generated '" . $this->backendPotFilename . "'" . PHP_EOL . PHP_EOL;
        return $this->backendPotFilename;
    }

    /**
    * Generates a .pot file in the same way as xgettext
    */
    public function generateProviderPotFile() {
        echo "Generating '" . $this->providerPotFilename . "'..." . PHP_EOL;
        $translatableStrings = $this->findTranslatableProviderStrings();
        $this->createOrUpdateProviderPot($translatableStrings);
        echo "Generated '" . $this->providerPotFilename . "'" . PHP_EOL . PHP_EOL;

        return $this->providerPotFilename;
    }

    private function addTranslatableProviderStringIfNotEmpty(&$translatableStrings, $json, $key, $providerFile) {
        if (isset($json[$key]) && !empty($json[$key])) {
            $translatableStrings[$providerFile][] = $json[$key];
        } else {
            echo "Note: in " . $providerFile . " " . $json[$this->nameKey] . " [" . $key . "] exists but is empty!" . PHP_EOL;
        }
    }

    private function addTranslatableFieldStringIfNotEmpty(&$translatableStrings, $json, $key, $providerName, $providerFile) {
        if (isset($json[$key]) && !empty($json[$key])) {
            $translatableStrings[$providerFile][] = $json[$key];
        } else {
            echo "Note: in " . $providerFile . " " . $providerName . " [" . $json[$this->nameKey] . "]" . " [" . $key . "] exists but is empty!" . PHP_EOL;
        }
    }

    private function findTranslatableProviderStrings() {
        $translatableStrings = array();
        $allProviderFiles = $this->getAllProviders();
        foreach ($allProviderFiles as $providerFile => $value) {
            foreach ($value as $provider) {
                if (!isset($provider[$this->nameKey]) || empty($provider[$this->nameKey])) {
                    var_dump($provider);
                    echo "Warning! ". $providerFile ." contains an unnamed provider, ignoring the provider and continuing\n";
                    continue;
                }

                $this->addTranslatableProviderStringIfNotEmpty($translatableStrings, $provider, $this->passwordHelpTextKey, $providerFile);
                $this->addTranslatableProviderStringIfNotEmpty($translatableStrings, $provider, $this->displayDescriptionKey, $providerFile);

                if (isset($provider[$this->fieldsKey])) {
                    foreach ($provider[$this->fieldsKey] as $field) {
                        $this->addTranslatableFieldStringIfNotEmpty($translatableStrings, $field, $this->hintKey, $provider[$this->nameKey], $providerFile);
                        $this->addTranslatableFieldStringIfNotEmpty($translatableStrings, $field, $this->helpTextKey, $provider[$this->nameKey], $providerFile);
                        $this->addTranslatableFieldStringIfNotEmpty($translatableStrings, $field, $this->descriptionKey, $provider[$this->nameKey], $providerFile);
                        $this->addTranslatableFieldStringIfNotEmpty($translatableStrings, $field, $this->patternErrorKey, $provider[$this->nameKey], $providerFile);
                    }
                }

                if (isset($provider[$this->supplementalFieldsKey])) {
                    foreach ($provider[$this->supplementalFieldsKey] as $field) {
                        $this->addTranslatableFieldStringIfNotEmpty($translatableStrings, $field, $this->hintKey, $provider[$this->nameKey], $providerFile);
                        $this->addTranslatableFieldStringIfNotEmpty($translatableStrings, $field, $this->helpTextKey, $provider[$this->nameKey], $providerFile);
                        $this->addTranslatableFieldStringIfNotEmpty($translatableStrings, $field, $this->descriptionKey, $provider[$this->nameKey], $providerFile);
                        $this->addTranslatableFieldStringIfNotEmpty($translatableStrings, $field, $this->patternErrorKey, $provider[$this->nameKey], $providerFile);
                    }
                }
            }
        }
        return $translatableStrings;
    }

    private function createOrUpdateProviderPot($translatableStrings) {
        $providerPot = fopen('../../po/' . $this->providerPotFilename, "w") or die("Unable to open .pot file!");

        $timezone = new \DateTimeZone("CEST");
        $date = new \DateTime('@' . time(), $timezone);
        $date->setTimezone($timezone);

        fwrite_line($providerPot, "# SOME DESCRIPTIVE TITLE.");
        fwrite_line($providerPot, "# Copyright (C) YEAR THE PACKAGE'S COPYRIGHT HOLDER");
        fwrite_line($providerPot, "# This file is distributed under the same license as the PACKAGE package.");
        fwrite_line($providerPot, "# FIRST AUTHOR <EMAIL@ADDRESS>, YEAR.");
        fwrite_line($providerPot, "#");
        fwrite_line($providerPot, "#, fuzzy");
        fwrite_line($providerPot, 'msgid ""');
        fwrite_line($providerPot, 'msgstr ""');
        fwrite_line($providerPot, '"Project-Id-Version: PACKAGE VERSION\n"');
        fwrite_line($providerPot, '"Report-Msgid-Bugs-To: \n"');
        fwrite_line($providerPot, '"POT-Creation-Date: ' . date_format($date, 'Y-m-d H:i') . '"');
        fwrite_line($providerPot, '"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\n"');
        fwrite_line($providerPot, '"Last-Translator: FULL NAME <EMAIL@ADDRESS>\n"');
        fwrite_line($providerPot, '"Language-Team: LANGUAGE <LL@li.org>\n"');
        fwrite_line($providerPot, '"Language: \n"');
        fwrite_line($providerPot, '"MIME-Version: 1.0\n"');
        fwrite_line($providerPot, '"Content-Type: text/plain; charset=UTF-8\n"');
        fwrite_line($providerPot, '"Content-Transfer-Encoding: 8bit\n"');
        fwrite_line($providerPot, '"Plural-Forms: nplurals=INTEGER; plural=EXPRESSION;\n"');

        fwrite_line($providerPot);

        foreach ($translatableStrings as $providerFile => $strings) {
            sort($strings);
            foreach ($strings as $translatableString) {
                if (empty($translatableString)) {
                    echo "Warning! Found empty string, continuing without adding the empty string to the .pot file" . PHP_EOL;
                    continue;
                }
                fwrite_line($providerPot, '# ' . $providerFile);
                fwrite_line($providerPot, 'msgid "' . str_replace('"', '\\"', $translatableString) . '"');
                fwrite_line($providerPot, "msgstr " . '""');
                fwrite_line($providerPot);
            }
        }

        fclose($providerPot);
    }

    private function getAllProviders() {
        $dir = $_SERVER["HOME"] . "/src/tink-backend/src/provider_configuration/data/seeding/";

        $allProviders = array();
        $it = new RecursiveDirectoryIterator($dir);
        foreach(new RecursiveIteratorIterator($it) as $filename)
        {
            if(preg_match('/.*available-providers.*/i', $filename)) {
                continue;
            }
            if(preg_match('/.*providers?-.*\.json/i', $filename)) {
                $providerFile = fopen($filename, "r") or die("Unable to open file!");
                $contents = str_replace('\\n', '\\\\n', fread($providerFile, filesize($filename)));
                $providerConf = json_decode($contents, true);

                if (isset($providerConf)) {
                    $cleanFilename = str_replace('../', '', $filename);
                    $providersKey = "providers";
                    $providersConfigKey = "provider-configuration";
                    if( array_key_exists($providersKey, $providerConf) ) {
                        $allProviders[$cleanFilename] = $providerConf[$providersKey];
                    } else if (array_key_exists($providersConfigKey, $providerConf)) {
                        $allProviders[$cleanFilename] = $providerConf[$providersConfigKey];
                    }
                }
                fclose($filename);
            }
        }
        return $allProviders;
    }
}

function fwrite_line(&$file, $line = "") {
    fwrite($file, $line . PHP_EOL);
}
?>
