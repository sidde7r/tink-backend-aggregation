package se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.entity.ConsentAccessEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {

    private ConsentAccessEntity access;
    private Boolean recurringIndicator;
    private String validUntil;
    private Integer frequencyPerDay;
    private Boolean combinedServiceIndicator;
}
