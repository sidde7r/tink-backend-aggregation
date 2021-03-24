package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AccountIdentifierPrefix;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreditorAccountEntity extends AccountEntity {

    CreditorAccountEntity(String accountNumber, AccountIdentifierType accountIdentifierType) {
        switch (accountIdentifierType) {
            case IBAN:
                this.iban = accountNumber;
                break;
            case SE:
                this.bban = getFormattedBBAN(accountNumber);
                break;
            case SE_BG:
                this.bban = AccountIdentifierPrefix.BANK_GIRO + accountNumber;
                break;
            case SE_PG:
                this.bban = AccountIdentifierPrefix.PLUS_GIRO + accountNumber;
                break;
            default:
                throw new IllegalStateException(
                        String.format(
                                ErrorMessages.INVALID_ACCOUNT_TYPE,
                                accountIdentifierType.toString()));
        }
    }

    private String getFormattedBBAN(String accountNumber) {
        NDAPersonalNumberIdentifier ssnIdentifier = new NDAPersonalNumberIdentifier(accountNumber);
        if (ssnIdentifier.isValid()) {
            return AccountIdentifierPrefix.PERSONAL_ACCOUNT
                    + ssnIdentifier.toSwedishIdentifier().getIdentifier();
        } else {
            return accountNumber;
        }
    }
}
