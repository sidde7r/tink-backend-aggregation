package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;

public abstract class AgentIntegrationTest {

    private final TestFileResourceReader testFileResourceReader;

    @Rule
    public WireMockRule wireMockRule =
            new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    protected AgentIntegrationTest(String moduleTestResourcesPath) {
        this.testFileResourceReader = new TestFileResourceReader(moduleTestResourcesPath);
    }

    String getTestResourceFileContent(String fileResourcePath) {
        return testFileResourceReader.readFileContent(fileResourcePath);
    }
}
