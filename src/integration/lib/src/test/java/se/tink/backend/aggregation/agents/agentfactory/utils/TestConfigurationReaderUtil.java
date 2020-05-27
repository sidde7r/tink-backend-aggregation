package se.tink.backend.aggregation.agents.agentfactory.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import se.tink.backend.aggregation.agents.agentfactory.AgentFactoryTestConfig;

@Getter
public class TestConfigurationReaderUtil {

    private final AgentFactoryTestConfig agentFactoryTestConfig;

    public TestConfigurationReaderUtil(String configurationPath) {
        FileInputStream configFileStream = null;
        try {
            configFileStream = new FileInputStream(new File(configurationPath));
            Yaml yaml = new Yaml(new Constructor(AgentFactoryTestConfig.class));
            yaml.setBeanAccess(BeanAccess.FIELD);
            this.agentFactoryTestConfig =
                    yaml.loadAs(configFileStream, AgentFactoryTestConfig.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
