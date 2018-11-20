package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;

public class DemoFileHandler {
    private static String generationBaseFile = DemoConstants.GENERATION_BASE_FILE;
    private final List<GenerationBase> generationBase;
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(DemoFileHandler.class);

    public DemoFileHandler(String basePath) {
        this.generationBase = loadGenerationBase(basePath + File.separator + generationBaseFile);
    }

    public List<GenerationBase> getGenerationBase() {
        return generationBase;
    }

    private List<GenerationBase> loadGenerationBase(String path) {
        File generationConfig = new File(path);

        try {
            return mapper.readValue(generationConfig, new TypeReference<List<GenerationBase>>(){});
        } catch (IOException e) {
            logger.info("Could not read the demo data generation base file. Verify that '%s' is present", path);
        }

        return Collections.emptyList();
    }


}
