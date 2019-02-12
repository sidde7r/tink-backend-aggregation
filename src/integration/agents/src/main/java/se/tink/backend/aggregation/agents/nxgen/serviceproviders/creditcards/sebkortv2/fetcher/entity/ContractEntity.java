package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
class ContractEntity {
    private String unInvoicedAmount;
    private String contractId;
    private String contractName;
    private String creditAmount;

    public String getUnInvoicedAmount() {
        return unInvoicedAmount;
    }

    public String getContractId() {
        return contractId;
    }

    public String getContractName() {
        return contractName;
    }

    public String getCreditAmount() {
        return creditAmount;
    }
}
