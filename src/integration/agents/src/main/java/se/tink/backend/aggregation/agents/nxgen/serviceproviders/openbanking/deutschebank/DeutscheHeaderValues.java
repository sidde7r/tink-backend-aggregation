package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DeutscheHeaderValues {
    private String redirectUrl;
    private String userIp;
}
