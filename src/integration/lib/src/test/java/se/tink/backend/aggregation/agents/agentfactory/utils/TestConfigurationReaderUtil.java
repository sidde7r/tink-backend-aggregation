package se.tink.backend.aggregation.agents.agentfactory.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

@Getter
public class TestConfigurationReaderUtil {

    private final AgentFactoryTestConfiguration agentFactoryTestConfiguration;

    public TestConfigurationReaderUtil(String configurationPath) {
        FileInputStream configFileStream = null;
        try {
            configFileStream = new FileInputStream(new File(configurationPath));
            Yaml yaml = new Yaml(new Constructor(AgentFactoryTestConfiguration.class));
            yaml.setBeanAccess(BeanAccess.FIELD);
            this.agentFactoryTestConfiguration =
                    yaml.loadAs(configFileStream, AgentFactoryTestConfiguration.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
