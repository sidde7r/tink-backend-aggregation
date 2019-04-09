package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

@JsonObject
public class RegisterPayeeRequest {
    private static final DefaultAccountIdentifierFormatter
            DEFAULT_ACCOUNT_IDENTIFIER_FORMATTER_FORMATTER =
                    new DefaultAccountIdentifierFormatter();

    private String type;
    private String accountNumber;
    private String name;

    private RegisterPayeeRequest(String type, String accountNumber, String name) {
        this.type = type;
        this.accountNumber = accountNumber;
        this.name = name;
    }

    public static RegisterPayeeRequest create(AccountIdentifier accountIdentifier, String name) {
        String type =
                accountIdentifier.is(AccountIdentifier.Type.SE_BG)
                        ? SwedbankBaseConstants.PaymentAccountType.BGACCOUNT
                        : SwedbankBaseConstants.PaymentAccountType.PGACCOUNT;
        String accountNumber =
                accountIdentifier.getIdentifier(DEFAULT_ACCOUNT_IDENTIFIER_FORMATTER_FORMATTER);

        return new RegisterPayeeRequest(type, accountNumber, name);
    }

    public String getType() {
        return type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }
}
