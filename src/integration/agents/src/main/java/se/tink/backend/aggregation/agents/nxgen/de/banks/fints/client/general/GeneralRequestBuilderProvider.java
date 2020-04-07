package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general.detail.DefaultRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general.detail.DeutscheBankRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general.detail.GeneralRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general.detail.IngDibaRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class GeneralRequestBuilderProvider {
    private static final ImmutableMap<Bank, GeneralRequestBuilder> DISPATCH_MAP =
            ImmutableMap.of(
                    Bank.ING_DIBA, new IngDibaRequestBuilder(),
                    Bank.DEUTSCHE_BANK, new DeutscheBankRequestBuilder());
    private static final GeneralRequestBuilder DEFAULT_REQUEST_BUILDER =
            new DefaultRequestBuilder();

    public static GeneralRequestBuilder getRequestBuilder(FinTsDialogContext dialogContext) {
        Bank bank = dialogContext.getConfiguration().getBank();
        return DISPATCH_MAP.getOrDefault(bank, DEFAULT_REQUEST_BUILDER);
    }
}
