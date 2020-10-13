package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnicreditBaseHeaderValues {
    private String redirectUrl;
    private String userIp;
}
