package se.tink.backend.aggregation.nxgen.propertiesloader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * <code>AgentPropertiesLoader</code> constructor expects a full class path of an agent.
 *
 * <p>By default, the <code>getName()</code> method of an agent class returns a shorten path, for
 * example: <code>
 * se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusAgent</code>
 *
 * <p>In order for a properties file to be found, there needs to be <code>
 * src.integration.agents.src.main.java</code> prefix added, so in result we get <code>
 * src.integration.agents.src.main.java.se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusAgent
 * </code>
 *
 * <p><code>AgentPropertiesLoader</code> by default searches for a properties file with a suffix
 * <code>-agent-prod.yaml</code> in the <code>
 * /resources</code> package of an agent.
 */
final class AgentPropertiesLoader {

    private static final String PROPERTIES_FILE_SUFFIX = "-agent-prod.yaml";
    private static final String RESOURCES_PACKAGE = "/resources";

    private final String agentClassPath;
    private final AgentPropertiesReader agentPropertiesReader;
    private final AgentPropertiesScanner agentPropertiesScanner;

    AgentPropertiesLoader(@NonNull String agentClassPath) {
        this.agentClassPath = agentClassPath;
        this.agentPropertiesReader = new AgentPropertiesReader();
        this.agentPropertiesScanner = new AgentPropertiesScanner();
    }

    public <T> T load(@NonNull Class<T> className) throws IOException {
        return loadPropertiesWithSuffix(className, PROPERTIES_FILE_SUFFIX);
    }

    private <T> T loadPropertiesWithSuffix(Class<T> className, String propertiesFileSuffix)
            throws IOException {
        File propertiesFile =
                agentPropertiesScanner
                        .scan(agentClassPath + RESOURCES_PACKAGE, propertiesFileSuffix).stream()
                        .collect(
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        files -> getPropertiesFile(files, propertiesFileSuffix)));

        return agentPropertiesReader.read(propertiesFile, className);
    }

    private File getPropertiesFile(List<File> files, String propertiesFileSuffix) {
        if (files.isEmpty()) {
            throw new AgentPropertiesLoaderException(
                    String.format(
                            "Couldn't find properties file with suffix [%s] in the agent's resources package! Path: %s",
                            propertiesFileSuffix, agentClassPath + RESOURCES_PACKAGE));
        } else if (files.size() > 1) {
            throw new AgentPropertiesLoaderException(
                    String.format(
                            "Found more than one file - [%s] with suffix [%s] in the agent's resources package! Path: %s",
                            files.size(),
                            propertiesFileSuffix,
                            agentClassPath + RESOURCES_PACKAGE));
        }
        return files.get(0);
    }
}
