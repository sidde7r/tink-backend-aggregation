package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
public class NickelAccount {
    private String accessToken;
    private String firstName;
    private String lastName;
    private String number;
    private Boolean primaryAccount;

    @JsonIgnore private NickelAccountDetails accountDetails;
    @JsonIgnore private NickelAccountOverview accountOverview;

    @JsonIgnore
    public Optional<TransactionalAccount> toTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        accountOverview.getBalance().movePointLeft(2), "EUR")))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountDetails.getIban())
                                .withAccountNumber(accountDetails.getIban())
                                .withAccountName(accountDetails.getIban())
                                .addIdentifier(new IbanIdentifier(accountDetails.getIban()))
                                .build())
                .addHolderName(accountDetails.getHolderName())
                .putInTemporaryStorage(StorageKeys.ACCESS_TKN, accessToken)
                .setApiIdentifier(number)
                .build();
    }

    @JsonIgnore
    public boolean isTransactional() {
        return true;
    }
}
