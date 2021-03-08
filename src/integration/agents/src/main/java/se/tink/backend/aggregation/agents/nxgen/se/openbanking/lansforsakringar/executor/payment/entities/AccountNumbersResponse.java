package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentTypes.DOMESTIC_CREDIT_TRANSFERS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentTypes.DOMESTIC_GIROS_RESPONSE;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier.Type;

@AllArgsConstructor
@NoArgsConstructor
@JsonObject
@Getter
public class AccountNumbersResponse {

    private List<AccountInfoEntity> accounts;

    public void checkIfTransactionTypeIsAllowed(String bban, Type accountIdentifierType)
            throws DebtorValidationException {

        AccountInfoEntity correctAccountIfPresent =
                getCorrectAccountIfPresent(bban)
                        .orElseThrow(
                                () ->
                                        new DebtorValidationException(
                                                ErrorMessages.DEBTOR_ACCOUNT_NOT_FOUND));

        switch (accountIdentifierType) {
            case SE_BG:
            case SE_PG:
                if (correctAccountIfPresent.getAllowedTransactionTypes().stream()
                        .noneMatch(type -> type.equals(DOMESTIC_GIROS_RESPONSE))) {
                    throw new DebtorValidationException(
                            ErrorMessages.ILLEGAL_PAYMENT_TYPE_FOR_DEBTOR_ACCOUNT);
                }
                break;
            case SE:
                if (correctAccountIfPresent.getAllowedTransactionTypes().stream()
                        .noneMatch(type -> type.equals(DOMESTIC_CREDIT_TRANSFERS_RESPONSE))) {
                    throw new DebtorValidationException(
                            ErrorMessages.ILLEGAL_PAYMENT_TYPE_FOR_DEBTOR_ACCOUNT);
                }
                break;
            default:
                throw new IllegalStateException(ErrorMessages.UNSUPPORTED_PAYMENT_TYPE);
        }
    }

    private Optional<AccountInfoEntity> getCorrectAccountIfPresent(String bban) {
        return accounts.stream().filter(a -> bban.equals(a.getBban())).findAny();
    }
}
