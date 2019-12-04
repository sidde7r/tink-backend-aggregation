package se.tink.sa.agent.pt.ob.sibs.rest.client.common;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonAccountSibsRequestRequest extends CommonSibsRequest {

    private String accountId;
    private Boolean isPsuInvolved;
}
