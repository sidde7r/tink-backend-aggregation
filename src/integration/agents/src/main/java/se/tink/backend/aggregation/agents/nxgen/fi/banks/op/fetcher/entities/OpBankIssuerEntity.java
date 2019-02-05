package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankIssuerEntity {
    private String branchNumber;
    private String bankNumber;
    private String bankName;
    private String branchName;

    public String getBranchNumber() {
        return branchNumber;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBranchName() {
        return branchName;
    }
}
