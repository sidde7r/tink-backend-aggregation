package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BalanceType {
    ACCOUNT("CLBD"),
    VALUE_DATE("VALU"),
    INSTANT("OTHR");

    private final String type;
}
