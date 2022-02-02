package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity;

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
