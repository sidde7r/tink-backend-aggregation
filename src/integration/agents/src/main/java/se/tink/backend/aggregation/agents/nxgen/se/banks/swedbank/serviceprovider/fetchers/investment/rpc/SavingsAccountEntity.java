package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsAccountEntity extends AbstractInvestmentAccountEntity {
    private boolean rightOfDisposal;

    public boolean isRightOfDisposal() {
        return rightOfDisposal;
    }
}
