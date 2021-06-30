package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ScaApproach;

@Getter
@RequiredArgsConstructor
public class AuthorisationInitiationDto {
    private final ScaApproach scaApproach;
}
