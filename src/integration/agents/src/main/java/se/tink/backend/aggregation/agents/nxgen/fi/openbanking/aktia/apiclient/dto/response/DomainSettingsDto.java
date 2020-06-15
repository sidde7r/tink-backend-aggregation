package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class DomainSettingsDto {

    private boolean pinTouchActivationAvailable;

    private String pinTouchTermsPdfUrl;

    private boolean ownTransferWithoutOtp;
}
