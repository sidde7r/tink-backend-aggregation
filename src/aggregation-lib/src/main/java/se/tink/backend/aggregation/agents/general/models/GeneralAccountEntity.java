package se.tink.backend.aggregation.agents.general.models;

import se.tink.libraries.account.AccountIdentifier;

public interface GeneralAccountEntity {

    AccountIdentifier generalGetAccountIdentifier();

    String generalGetBank();

    String generalGetName();
}
