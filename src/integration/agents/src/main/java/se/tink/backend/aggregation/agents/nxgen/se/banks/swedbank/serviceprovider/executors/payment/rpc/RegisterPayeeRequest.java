package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.PaymentAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

@JsonObject
@Getter
@AllArgsConstructor
public class RegisterPayeeRequest {
    private static final DefaultAccountIdentifierFormatter
            DEFAULT_ACCOUNT_IDENTIFIER_FORMATTER_FORMATTER =
                    new DefaultAccountIdentifierFormatter();

    private String type;
    private String accountNumber;
    private String name;

    public static RegisterPayeeRequest create(AccountIdentifier accountIdentifier, String name) {
        String type =
                accountIdentifier.is(AccountIdentifierType.SE_BG)
                        ? PaymentAccountType.BGACCOUNT
                        : PaymentAccountType.PGACCOUNT;
        String accountNumber =
                accountIdentifier.getIdentifier(DEFAULT_ACCOUNT_IDENTIFIER_FORMATTER_FORMATTER);

        return new RegisterPayeeRequest(type, accountNumber, name);
    }
}
