package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsAccountEntity extends AbstractInvestmentAccountEntity {
    private boolean rightOfDisposal;

    public boolean isRightOfDisposal() {
        return rightOfDisposal;
    }
}
