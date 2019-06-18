package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import java.util.ArrayList;
import java.util.List;

public abstract class AccessEntity {

    protected List<IbanEntity> accounts = new ArrayList<>();
    protected List<IbanEntity> transactions = new ArrayList<>();
    protected List<IbanEntity> balances = new ArrayList<>();

    public abstract void addIban(final String iban);

    public abstract void addIbans(final List<String> ibans);
}
