package se.tink.backend.aggregation.nxgen.propertiesloader;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

final class AgentPropertiesScanner {

    Optional<File> scan(String resourcesPackagePath, String propertiesFileSuffix) {
        return Optional.ofNullable(new File(resourcesPackagePath).listFiles())
                .flatMap(files -> findPropertiesFile(propertiesFileSuffix, files));
    }

    private Optional<File> findPropertiesFile(String propertiesFileSuffix, File[] files) {
        return Arrays.stream(files)
                .filter(file -> file.getName().endsWith(propertiesFileSuffix))
                .findFirst();
    }
}
