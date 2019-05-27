package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;

public abstract class AbstractBankIdSignResponse extends AbstractBankIdResponse {
    private String signingStatus;
    private LinkEntity imageChallengeData;
    private boolean qrCodeSigning;

    public boolean isQrCodeSigning() {
        return qrCodeSigning;
    }

    public LinkEntity getImageChallengeData() {
        return imageChallengeData;
    }

    @JsonIgnore
    public SwedbankBaseConstants.BankIdResponseStatus getBankIdStatus() {
        return SwedbankBaseConstants.BankIdResponseStatus.fromStatusCode(signingStatus);
    }

    public String getSigningStatus() {
        return signingStatus;
    }
}
