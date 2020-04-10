package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog.detail.DefaultRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog.detail.DeutscheBankRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog.detail.DialogRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog.detail.IngDibaRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DialogRequestBuilderProvider {
    private static final ImmutableMap<Bank, DialogRequestBuilder> DISPATCH_MAP =
            ImmutableMap.of(
                    Bank.ING_DIBA, new IngDibaRequestBuilder(),
                    Bank.DEUTSCHE_BANK, new DeutscheBankRequestBuilder());
    private static final DialogRequestBuilder DEFAULT_REQUEST_BUILDER = new DefaultRequestBuilder();

    public static DialogRequestBuilder getRequestBuilder(FinTsDialogContext dialogContext) {
        Bank bank = dialogContext.getConfiguration().getBank();
        return DISPATCH_MAP.getOrDefault(bank, DEFAULT_REQUEST_BUILDER);
    }
}
