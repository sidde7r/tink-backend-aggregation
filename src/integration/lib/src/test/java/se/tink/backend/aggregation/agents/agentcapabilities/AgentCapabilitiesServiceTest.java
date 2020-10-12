package se.tink.backend.aggregation.agents.agentcapabilities;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;

public class AgentCapabilitiesServiceTest {

    @Rule public ErrorCollector collector = new ErrorCollector();

    private static final String EXPECTED_AGENT_CAPABILITIES_FILE_PATH =
            "external/tink_backend/src/provider_configuration/data/seeding/providers/capabilities/agent-capabilities.json";

    private static final Set<String>
            AGENTS_THAT_DO_NOT_EXISTS_BUT_ARE_LISTED_IN_AGENT_CAPABILITIES_JSON_FILE =
                    newHashSet(
                            "nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisAgent",
                            "nxgen.se.openbanking.volvofinans.VolvoFinansAgent",
                            "nxgen.it.openbanking.bancoposta.BancoPostaAgent",
                            "banks.fi.op.OsuuspankkiAgent",
                            "nxgen.uk.openbanking.bankofireland.BankOfIrelandAgent",
                            "banks.sdc.v7.SDCV7Agent",
                            "banks.fi.alandsbanken.AlandsBankenAgent",
                            "banks.ie.aib.AibAgent",
                            "banks.ICABankenAgent",
                            "banks.ro.bcr.BCRAgent",
                            "banks.pl.pko.PkoBankPolskiAgent",
                            "nxgen.demo.banks.multisupplemental.MultiSupplementalAgent",
                            "banks.swedbank.SwedbankAPIAgent",
                            "nxgen.uk.openbanking.modelo.ModeloAgent",
                            "nxgen.uk.openbanking.tide.TideBusinessAgent",
                            "banks.dk.bankdata.BankDataAgent",
                            "banks.pl.mbank.MBankAgent",
                            "banks.swedbank.SwedbankAgent",
                            "banks.handelsbanken.v6.HandelsbankenV6Agent",
                            "banks.gr.nationalbank.NationalBankAgent",
                            "banks.citibank.v11.CitibankV11Agent",
                            "banks.sk.slovenskasporitelna.SlovenskaSporitelnaAgent",
                            "nxgen.ie.openbanking.kbc.KbcIrelandAgent");

    private Map<String, Set<String>> expectedCapabilitiesMap;
    private Map<String, Set<Capability>> capabilities;

    private Function<Entry<String, List<String>>, Set<String>> convertFromListToSet =
            entry -> new HashSet<>(entry.getValue());

    @Before
    public void init() {
        expectedCapabilitiesMap =
                readExpectedAgentCapabilities(EXPECTED_AGENT_CAPABILITIES_FILE_PATH);

        capabilities = new AgentCapabilitiesService().getAgentsCapabilities();
        removeTestAgents();
    }

    private void removeTestAgents() {
        capabilities.remove("agentcapabilities.TestAgentImplementingExecutors");
        capabilities.remove("agentcapabilities.TestAgentWithListedCapabilities");
    }

    @Test
    public void shouldComputeCapabilitiesExactlyAsInAgentCapabilitiesJsonFile() {
        expectedCapabilitiesMap.forEach(
                (agentName, expectedCapabilities) -> {
                    try {
                        if (!capabilities.containsKey(agentName)) {
                            throw new AssertionError(
                                    "agent: " + agentName + " doesnt exist in the source code");
                        }

                        Set<String> computedCapabilities = getCapabilitiesAsSet(agentName);
                        assertThat(computedCapabilities)
                                .as(
                                        "for agent %s\n expected capabilities are %s,\n but found: \n%s",
                                        agentName, expectedCapabilities, computedCapabilities)
                                .isEqualTo(expectedCapabilities);
                    } catch (AssertionError t) {
                        // get rid of useless stack trace
                        t.setStackTrace(new StackTraceElement[0]);
                        collector.addError(t);
                    }
                });
    }

    private Set<String> getCapabilitiesAsSet(String agentName) {
        return capabilities.get(agentName).stream().map(Enum::name).collect(Collectors.toSet());
    }

    private Map<String, Set<String>> readExpectedAgentCapabilities(String filePath) {
        try {
            byte[] agentCapabilitiesFileData = Files.readAllBytes(Paths.get(filePath));
            Map<String, List<String>> capabilitiesMap =
                    new ObjectMapper().readValue(new String(agentCapabilitiesFileData), Map.class);
            AGENTS_THAT_DO_NOT_EXISTS_BUT_ARE_LISTED_IN_AGENT_CAPABILITIES_JSON_FILE.forEach(
                    capabilitiesMap::remove);
            return capabilitiesMap.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, convertFromListToSet));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
