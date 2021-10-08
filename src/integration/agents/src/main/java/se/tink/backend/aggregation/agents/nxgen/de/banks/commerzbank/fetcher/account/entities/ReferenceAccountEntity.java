package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ReferenceAccountEntity {
    private String accountOwnerName;
    private String bankCode;
    private String bic;
    private Object currency;
    private String externalAccountNumber;
    private String iban;
    private String linkedAccount;
    private Object technicalAccountNumber;
    private Object productBranch;
    private Object referenceAccountString;
}
