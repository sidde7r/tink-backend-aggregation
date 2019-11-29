package se.tink.sa.agent.pt.ob.sibs.rest.client.common;

import lombok.*;

@Getter
@Setter
@Builder
public class CommonSibsRequestRequest {

    private String bankCode;
    private String consentId;
}
