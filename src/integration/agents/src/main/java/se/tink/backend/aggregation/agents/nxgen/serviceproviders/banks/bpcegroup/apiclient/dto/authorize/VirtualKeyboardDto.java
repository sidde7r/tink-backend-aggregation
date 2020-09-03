package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class VirtualKeyboardDto {

    private String externalRestMediaApiUrl;

    private int width;

    private boolean base64;

    private boolean audio;

    private boolean secure;

    private int height;
}
