package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.PaymentTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonObject
@Getter
public class AccountNumbersResponse {

    private final List<AccountInfoEntity> accounts;

    @JsonCreator
    public AccountNumbersResponse(@JsonProperty("accounts") List<AccountInfoEntity> accounts) {
        this.accounts = accounts;
    }

    public Optional<AccountInfoEntity> findAccountInfoEntity(String accountNumber) {
        return accounts.stream().filter(a -> a.getBban().equals(accountNumber)).findFirst();
    }

    public void checkIfTransactionTypeIsAllowed(
            String bban, AccountIdentifierType accountIdentifierType)
            throws DebtorValidationException {

        AccountInfoEntity correctAccountIfPresent =
                findAccountInfoEntity(bban)
                        .orElseThrow(
                                () ->
                                        new DebtorValidationException(
                                                DebtorValidationException.DEFAULT_MESSAGE));

        switch (accountIdentifierType) {
            case SE_BG:
            case SE_PG:
                if (correctAccountIfPresent.getAllowedTransactionTypes().stream()
                        .noneMatch(type -> type.equals(PaymentTypes.DOMESTIC_GIROS_RESPONSE))) {
                    throw new DebtorValidationException(DebtorValidationException.DEFAULT_MESSAGE);
                }
                break;
            case SE:
                if (correctAccountIfPresent.getAllowedTransactionTypes().stream()
                        .noneMatch(
                                type ->
                                        type.equals(
                                                PaymentTypes.DOMESTIC_CREDIT_TRANSFERS_RESPONSE))) {
                    throw new DebtorValidationException(DebtorValidationException.DEFAULT_MESSAGE);
                }
                break;
            default:
                throw new IllegalStateException(ErrorMessages.UNSUPPORTED_PAYMENT_TYPE);
        }
    }
}
