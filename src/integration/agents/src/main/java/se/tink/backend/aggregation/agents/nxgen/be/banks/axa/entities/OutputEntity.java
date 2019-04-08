package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaAccountTransactionsEntityDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaAccountsDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaErrorsDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.AccountTransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class OutputEntity {
    private String activationPassword;
    private String challenge;
    private String encryptedNonces;
    private String encryptedServerPublicKey;
    private String serialNo;
    private String serverInitialVector;
    private String serverTime;
    private String xfad;

    @JsonDeserialize(using = AxaErrorsDeserializer.class)
    private List<ErrorsEntity> errors;

    private String result;

    @JsonDeserialize(using = AxaAccountsDeserializer.class)
    private List<AccountEntity> accounts;

    private Boolean hasPensionsSavingsAccount;
    private Boolean hasSecuritiesAccount;

    @JsonDeserialize(using = AxaAccountTransactionsEntityDeserializer.class)
    private AccountTransactionsEntity accountTransactions;

    public List<ErrorsEntity> getErrors() {
        return errors;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public String getServerTime() {
        return serverTime;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getActivationPassword() {
        return activationPassword;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public String getXfad() {
        return xfad;
    }

    public String getServerInitialVector() {
        return serverInitialVector;
    }

    public String getEncryptedNonces() {
        return encryptedNonces;
    }

    public String getEncryptedServerPublicKey() {
        return encryptedServerPublicKey;
    }

    public AccountTransactionsEntity getAccountTransactions() {
        return accountTransactions;
    }
}
