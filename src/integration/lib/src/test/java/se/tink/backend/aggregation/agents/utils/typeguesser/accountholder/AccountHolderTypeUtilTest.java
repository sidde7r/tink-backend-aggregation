package se.tink.backend.aggregation.agents.utils.typeguesser.accountholder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.FinancialService;
import se.tink.backend.agents.rpc.FinancialService.FinancialServiceSegment;
import se.tink.backend.agents.rpc.Provider;

public class AccountHolderTypeUtilTest {

    @Test
    public void shouldReturnPersonalIfPersonalSegmentsOnly() {
        // given
        Provider provider = new Provider();
        List<FinancialService> services =
                Arrays.asList(
                        createFinancialService(
                                "Personal Banking", FinancialServiceSegment.PERSONAL),
                        createFinancialService(
                                "Persona Wealth Banking", FinancialServiceSegment.PERSONAL));
        provider.setFinancialServices(services);

        // when
        AccountHolderType accountHolderType = AccountHolderTypeUtil.inferHolderType(provider);

        // then
        assertThat(accountHolderType).isEqualTo(AccountHolderType.PERSONAL);
    }

    @Test
    public void shouldReturnBusinessIfBusinessSegmentsOnly() {
        // given
        Provider provider = new Provider();
        List<FinancialService> services =
                Collections.singletonList(
                        createFinancialService(
                                "Business Banking", FinancialServiceSegment.BUSINESS));
        provider.setFinancialServices(services);

        // when
        AccountHolderType accountHolderType = AccountHolderTypeUtil.inferHolderType(provider);

        // then
        assertThat(accountHolderType).isEqualTo(AccountHolderType.BUSINESS);
    }

    @Test
    public void shouldReturnUnknownIfBothSegments() {
        Provider provider = new Provider();
        List<FinancialService> services =
                Arrays.asList(
                        createFinancialService(
                                "Personal Banking", FinancialServiceSegment.PERSONAL),
                        createFinancialService(
                                "Business Banking", FinancialServiceSegment.BUSINESS));
        provider.setFinancialServices(services);

        AccountHolderType accountHolderType = AccountHolderTypeUtil.inferHolderType(provider);
        assertThat(accountHolderType).isEqualTo(AccountHolderType.UNKNOWN);
    }

    private FinancialService createFinancialService(String name, FinancialServiceSegment segment) {
        return new FinancialService().setShortName(name).setSegment(segment);
    }
}
