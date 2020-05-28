package se.tink.backend.aggregation.agents.agentfactory.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

public class TestConfigurationReader {

    private final Yaml yaml;

    public TestConfigurationReader() {
        yaml = new Yaml(new Constructor(AgentFactoryTestConfiguration.class));
        yaml.setBeanAccess(BeanAccess.FIELD);
    }

    public AgentFactoryTestConfiguration readConfiguration(String configurationPath) {
        try (FileInputStream configFileStream = new FileInputStream(new File(configurationPath))) {
            return yaml.loadAs(configFileStream, AgentFactoryTestConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
