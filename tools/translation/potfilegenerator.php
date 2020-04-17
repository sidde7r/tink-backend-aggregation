<?php
class PotFileGenerator {
    private $backendPotFilename = "tink-aggr-backend.pot";

    public function generateBackendPotFile($root) {
        chdir($root);
        echo "Generating '" . $this->backendPotFilename . "'..." . PHP_EOL;
        exec ('find src/ -name \'*.java\' ! -name \'*Test.java\' -exec xgettext --from-code="UTF-8" -kgetString -kgetPluralString:1,2 -kLocalizableKey -kLocalizableParametrizedKey -kLocalizablePluralKey:1,2 --join-existing -o po/' . $this->backendPotFilename . ' {} +');
        echo "Generated '" . $this->backendPotFilename . "'" . PHP_EOL . PHP_EOL;
        return $this->backendPotFilename;
    }

}
?>
