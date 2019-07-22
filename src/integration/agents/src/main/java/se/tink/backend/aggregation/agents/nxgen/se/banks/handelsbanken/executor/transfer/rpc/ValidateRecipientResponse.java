package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.TransferAmount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignRequest.AmountableDestination;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class ValidateRecipientResponse extends TransferableResponse
        implements AmountableDestination {

    private String accountNumber;
    private String accountNumberFormatted;
    private String bankName;

    @Override
    public TransferAmount toTransferAmount() {
        this.accountNumberFormatted = accountNumberFormatted.replace(" ", "");
        return TransferAmount.from(accountNumber, accountNumberFormatted, bankName);
    }

    @Override
    public boolean isKnownDestination() {
        return false;
    }

    @JsonIgnore
    public Optional<URL> toCreatable() {
        return searchLink(HandelsbankenConstants.URLS.Links.CREATE);
    }
}
