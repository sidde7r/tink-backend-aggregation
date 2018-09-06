<?php
class PotFileGenerator {
    private $backendPotFilename = "tink-backend.pot";
    private $providerPotFilename = "tink-provider.pot";

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

    private function findTranslatableProviderStrings() {
        $translatableStrings = array();

        foreach ($this->getAllProviders() as $provider) {
            if (!isset($provider->name) || empty($provider->name)) {
                echo "Warning! Found unnamed provider, ignoring the provider and continuing";
                continue;
            }

            if (isset($provider->passwordHelpText)) {
                if (!empty($provider->passwordHelpText)) {
                    $translatableStrings[] = $provider->passwordHelpText;
                } else {
                    echo "Note: " . $provider->name . "['passwordHelpText'] exists but is empty!" . PHP_EOL;
                }
            }
            if (isset($provider->displayDescription)) {
                if (!empty($provider->displayDescription)) {
                    $translatableStrings[] = $provider->displayDescription;
                } else {
                    echo "Note: " . $provider->name . "['displayDescription'] exists but is empty!" . PHP_EOL;
                }
            }

            if (isset($provider->fields)) {
                foreach ($provider->fields as $field) {
                    if (isset($field->hint)) {
                        if (!empty($field->hint)) {
                            $translatableStrings[] = $field->hint;
                        } else {
                            echo "Note: " . $provider->name . "['" . $field->name . "']['hint'] exists but is empty!" . PHP_EOL;
                        }
                    }
                    if (isset($field->helpText)) {
                        if (!empty($field->helpText)) {
                            $translatableStrings[] = $field->helpText;
                        } else {
                            echo "Note: " . $provider->name . "['" . $field->name . "']['helpText'] exists but is empty!" . PHP_EOL;
                        }
                    }
                    if (isset($field->description)) {
                        if (!empty($field->description)) {
                            $translatableStrings[] = $field->description;
                        } else {
                            echo "Note: " . $provider->name . "['" . $field->name . "']['description'] exists but is empty!" . PHP_EOL;
                        }
                    }
                    if (isset($field->patternError)) {
                        if (!empty($field->patternError)) {
                            $translatableStrings[] = $field->patternError;
                        } else {
                            echo "Note: " . $provider->name . "['" . $field->name . "']['patternError'] exists but is empty!" . PHP_EOL;
                        }
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
        fwrite_line($providerPot, "#, java-format");

        foreach ($translatableStrings as $translatableString) {
            if (empty($translatableString)) {
                echo "Warning! Found empty string, continuing without adding the empty string to the .pot file" . PHP_EOL;
                continue;
            }
            fwrite_line($providerPot, 'msgid "' . str_replace('"', '\\"', $translatableString) . '"');
            fwrite_line($providerPot, "msgstr " . '""');
            fwrite_line($providerPot);
        }

        fclose($providerPot);
    }

    private function getAllProviders() {
        $dir = "../../data/seeding/";
        $allProviders = array();

        foreach (glob($dir . "providers-se.json") as $filename) {
            $providerFile = fopen($filename, "r") or die("Unable to open file!");
            $providerConf = json_decode(fread($providerFile, filesize($filename)));

            $allProviders = array_merge($allProviders, $providerConf->providers);
            fclose($providerFile);
        }

        return $allProviders;
    }
}

function fwrite_line(&$file, $line = "") {
    fwrite($file, $line . PHP_EOL);
}
?>
