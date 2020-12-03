package se.tink.backend.agents.rpc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import junit.framework.TestCase;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

public class BalanceTypeCompatibilityTest {

    @Test
    public void testForEnumValuesEquality() {
        List<String> agentRpcBalanceTypes =
                Arrays.asList(BalanceType.values()).stream()
                        .map(e -> e.name())
                        .collect(Collectors.toList());
        List<String> librariesRpcBalanceTypes =
                Arrays.asList(se.tink.libraries.account.enums.BalanceType.values()).stream()
                        .map(e -> e.name())
                        .collect(Collectors.toList());
        List<String> coreAccountBalanceTypes =
                Arrays.asList(se.tink.backend.aggregation.nxgen.core.account.BalanceType.values())
                        .stream()
                        .map(e -> e.name())
                        .collect(Collectors.toList());

        TestCase.assertEquals(agentRpcBalanceTypes.size(), librariesRpcBalanceTypes.size());
        TestCase.assertEquals(agentRpcBalanceTypes.size(), coreAccountBalanceTypes.size());

        TestCase.assertTrue(
                CollectionUtils.containsAll(agentRpcBalanceTypes, librariesRpcBalanceTypes));
        TestCase.assertTrue(
                CollectionUtils.containsAll(agentRpcBalanceTypes, coreAccountBalanceTypes));
    }
}
