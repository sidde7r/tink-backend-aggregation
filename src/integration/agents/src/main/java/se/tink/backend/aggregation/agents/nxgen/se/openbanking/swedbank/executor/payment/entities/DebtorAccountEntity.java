package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier.Type;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class DebtorAccountEntity extends AccountEntity {

  DebtorAccountEntity(String accountNumber, Type accountIdentifierType) {
    switch (accountIdentifierType) {
      case IBAN:
        this.iban = accountNumber;
        break;
      case SE:
        this.bban = accountNumber;
        break;
      default:
        throw new IllegalStateException(
            String.format(
                ErrorMessages.INVALID_ACCOUNT_TYPE,
                accountIdentifierType.toString()));
    }
  }
}
