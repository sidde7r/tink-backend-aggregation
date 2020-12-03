package se.tink.backend.agents.rpc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import junit.framework.TestCase;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

public class CreditLineTypeCompatibilityTest {

    @Test
    public void testForEnumValuesEquality() {
        List<String> agentRpcCreditLineTypes =
                Arrays.asList(CreditLineType.values()).stream()
                        .map(e -> e.name())
                        .collect(Collectors.toList());
        List<String> librariesRpcCreditLineTypes =
                Arrays.asList(se.tink.libraries.account.enums.CreditLineType.values()).stream()
                        .map(e -> e.name())
                        .collect(Collectors.toList());
        List<String> coreAccountCreditLineTypes =
                Arrays.asList(
                                se.tink.backend.aggregation.nxgen.core.account.CreditLineType
                                        .values())
                        .stream()
                        .map(e -> e.name())
                        .collect(Collectors.toList());

        TestCase.assertEquals(agentRpcCreditLineTypes.size(), librariesRpcCreditLineTypes.size());
        TestCase.assertEquals(agentRpcCreditLineTypes.size(), coreAccountCreditLineTypes.size());

        TestCase.assertTrue(
                CollectionUtils.containsAll(agentRpcCreditLineTypes, librariesRpcCreditLineTypes));
        TestCase.assertTrue(
                CollectionUtils.containsAll(agentRpcCreditLineTypes, coreAccountCreditLineTypes));
    }
}
