package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class ValidationUnitResponseItemDto {

    private VirtualKeyboardDto virtualKeyboard;

    private String id;

    private String type;

    private String scope;

    private String phoneNumber;

    private int minSize;

    private int maxSize;
}
