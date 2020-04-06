package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general;

import java.util.HashMap;
import java.util.Map;
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
    private static final Map<Bank, GeneralRequestBuilder> DISPATCH_MAP = new HashMap<>();
    private static final GeneralRequestBuilder DEFAULT_REQUEST_BUILDER =
            new DefaultRequestBuilder();

    public static GeneralRequestBuilder getRequestBuilder(FinTsDialogContext dialogContext) {
        Bank bank = dialogContext.getConfiguration().getBank();
        return DISPATCH_MAP.getOrDefault(bank, DEFAULT_REQUEST_BUILDER);
    }

    static {
        DISPATCH_MAP.put(Bank.ING_DIBA, new IngDibaRequestBuilder());
        DISPATCH_MAP.put(Bank.DEUTSCHE_BANK, new DeutscheBankRequestBuilder());
    }
}
