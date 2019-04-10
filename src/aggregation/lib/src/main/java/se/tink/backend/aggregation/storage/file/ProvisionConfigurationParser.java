package se.tink.backend.aggregation.storage.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import se.tink.backend.aggregation.storage.file.models.ProvisionClientsConfig;

public class ProvisionConfigurationParser {
    // TODO: make this configurable from the ./charts/tink-backend-aggregation/templates/aggregation-config.yaml
    // TODO: consider if all configuration should live in ./charts/tink-backend-aggregation/values/aggregation-production.yaml
    private static final String PROVISION_CLIENT_CONFIGURATION_FILE = "data/provision/clients/config.yaml";
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static ProvisionClientsConfig parse() throws IOException {
        File f = new File(PROVISION_CLIENT_CONFIGURATION_FILE);

        return mapper.readValue(f, ProvisionClientsConfig.class);
    }
}
