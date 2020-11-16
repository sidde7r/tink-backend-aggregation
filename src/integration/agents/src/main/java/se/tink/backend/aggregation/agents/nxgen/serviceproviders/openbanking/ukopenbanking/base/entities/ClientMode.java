package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;

@Getter
@RequiredArgsConstructor
public enum ClientMode {
    ACCOUNTS(UkOpenBankingV31Constants.Scopes.ACCOUNTS),
    PAYMENTS(UkOpenBankingV31Constants.Scopes.PAYMENTS);

    private final String value;
}
