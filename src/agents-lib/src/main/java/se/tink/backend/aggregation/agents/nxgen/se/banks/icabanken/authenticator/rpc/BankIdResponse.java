package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdResponse extends BaseResponse<BankIdBodyEntity> {
    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(BankIdResponse.class);

    public BankIdStatus getBankIdStatus() {
        String status = Preconditions.checkNotNull(getBody().getStatus(), "BankID status was null");

        switch (status.toLowerCase()) {
            case IcaBankenConstants.BankIdStatus.OK:
                return BankIdStatus.DONE;
            case IcaBankenConstants.BankIdStatus.PENDING:
                return BankIdStatus.WAITING;
            case IcaBankenConstants.BankIdStatus.ABORTED:
                return BankIdStatus.CANCELLED;
            default:
                log.warn("Unknown bankID status: {}", status);
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
