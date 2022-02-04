package se.tink.backend.aggregation.nxgen.propertiesloader;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class AgentPropertiesScanner {

    List<File> scan(String resourcesPackagePath, String propertiesFileSuffix) {
        return Optional.ofNullable(new File(resourcesPackagePath).listFiles())
                .map(files -> findPropertiesFiles(propertiesFileSuffix, files))
                .orElse(Collections.emptyList());
    }

    private List<File> findPropertiesFiles(String propertiesFileSuffix, File[] files) {
        return Arrays.stream(files)
                .filter(file -> file.getName().endsWith(propertiesFileSuffix))
                .collect(Collectors.toList());
    }
}
