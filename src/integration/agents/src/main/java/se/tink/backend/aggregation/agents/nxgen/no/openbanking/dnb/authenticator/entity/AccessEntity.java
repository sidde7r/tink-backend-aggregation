package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private final List<IbanEntity> accounts = new ArrayList<>();
    private final List<IbanEntity> transactions = new ArrayList<>();
    private final List<IbanEntity> balances = new ArrayList<>();
}
