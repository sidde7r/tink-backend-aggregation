package se.tink.backend.integration.boot.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class ConfigurationUtils {
    public static <T> T getConfiguration(String fileName, Class<T> cls) throws FileNotFoundException {
        FileInputStream configFileStream = new FileInputStream(new File(fileName));
        Yaml yaml = new Yaml(new Constructor(cls));
        return cls.cast(yaml.load(configFileStream));
    }
}
