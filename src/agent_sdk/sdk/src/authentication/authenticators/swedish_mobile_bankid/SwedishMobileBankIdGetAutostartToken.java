package se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid;

import se.tink.agent.sdk.storage.Reference;
import se.tink.agent.sdk.user_interaction.SwedishMobileBankIdInfo;

public interface SwedishMobileBankIdGetAutostartToken {
    SwedishMobileBankIdInfo getAutostartToken(Reference reference);
}
