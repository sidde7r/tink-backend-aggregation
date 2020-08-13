package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaAccountTransactionsEntityDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaAccountsDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaErrorsDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.AccountTransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
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
}
