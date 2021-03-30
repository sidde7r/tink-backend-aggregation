package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BalanceType {
    CLBD("CLBD"),
    XPCD("XPCD"),
    VALU("VALU"),
    OTHR("OTHR");

    private final String type;
}
