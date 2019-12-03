package se.tink.sa.agent.pt.ob.sibs.rest.client.common;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonSibsRequest {

    private String bankCode;
    private String consentId;
}
