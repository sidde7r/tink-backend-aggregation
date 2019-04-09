package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities.AddressEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String id;
    private String currency;
    private String bankName;
    private String beneficiaryName;
    private String accountNumber;
    private String sortCode;
    private String transferTime;
    private boolean personal;
    private AddressEntity address;
    private boolean local;
    private boolean activate;
    private String iban;
    private String bic;
    private String requiredReference;

    public String getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBankName() {
        return bankName;
    }

    public Optional<String> getBeneficiaryName() {
        return Optional.ofNullable(beneficiaryName);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }

    public String getTransferTime() {
        return transferTime;
    }

    public boolean isPersonal() {
        return personal;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isActivate() {
        return activate;
    }

    public Optional<String> getIban() {
        return Optional.ofNullable(iban);
    }

    public String getBic() {
        return bic;
    }

    public String getRequiredReference() {
        return requiredReference;
    }
}
