package se.tink.agent.sdk.utils;

import se.tink.agent.sdk.user_interaction.SupplementalInformation;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;

/** Get Fields registered in the Provider as supplementalFields. */
public interface SupplementalInformationHelper {

    /**
     * @param keys One or more field keys
     * @return SupplementalInformation which can be modified before constructing a UserInteraction
     * @throws SupplementalInfoException If any of the field keys cannot be found.
     */
    SupplementalInformation getMutableFields(Field.Key... keys) throws SupplementalInfoException;

    UserInteraction<SupplementalInformation> getFields(Field.Key... keys)
            throws SupplementalInfoException;

    UserInteraction<SupplementalInformation> getLoginInput() throws SupplementalInfoException;

    UserInteraction<SupplementalInformation> getOtpInput() throws SupplementalInfoException;

    UserInteraction<SupplementalInformation> getLoginChallengeResponse(String challenge)
            throws SupplementalInfoException;

    UserInteraction<SupplementalInformation> getSignCodeChallengeResponse(String challenge)
            throws SupplementalInfoException;
}
