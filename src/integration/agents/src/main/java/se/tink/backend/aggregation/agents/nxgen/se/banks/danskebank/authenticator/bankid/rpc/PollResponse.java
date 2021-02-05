package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractBankIdResponse;

public class PollResponse extends AbstractBankIdResponse {

    @JsonIgnore
    public BankIdStatus getBankIdStatus() {
        if (getResponseCode() == 504) {
            return BankIdStatus.WAITING;
        } else if (getStatus() == null || getResponseCode() == 500) {
            throw new BankServiceException(BankServiceError.BANK_SIDE_FAILURE);
        }

        switch (getStatus().toLowerCase()) {
            case DanskeBankSEConfiguration.BankIdStatus.OK:
                return BankIdStatus.DONE;
            case DanskeBankSEConfiguration.BankIdStatus.STARTED:
            case DanskeBankSEConfiguration.BankIdStatus.OUTSTANDING_TRANSACTION:
            case DanskeBankSEConfiguration.BankIdStatus.USER_SIGN:
            case DanskeBankSEConfiguration.BankIdStatus.NO_CLIENT:
                return BankIdStatus.WAITING;
            case DanskeBankSEConfiguration.BankIdStatus.CANCELLED:
                return BankIdStatus.INTERRUPTED;
            case DanskeBankSEConfiguration.BankIdStatus.USER_CANCEL:
                return BankIdStatus.CANCELLED;
            case DanskeBankSEConfiguration.BankIdStatus.EXPIRED_TRANSACTION:
                return BankIdStatus.TIMEOUT;
            default:
                throw new IllegalStateException(getStatus());
        }
    }
}
