package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class ConfirmEnrollRequestBody extends MultivaluedMapImpl {

    public ConfirmEnrollRequestBody(
            final String ingId,
            final String signingId,
            final String otpValue,
            final String otpSystem,
            final String deviceId,
            final String aggregator) {

        add(IngConstants.Storage.ING_ID, ingId);
        add(IngConstants.Session.SIGNING_ID, signingId);
        add(IngConstants.Session.OTP_VALUE, otpValue);
        add(IngConstants.Session.OTP_SYSTEM, otpSystem);
        add(
                IngConstants.Session.ValuePairs.FLAG_TRUSTED_BENIFICIARIES.getKey(),
                IngConstants.Session.ValuePairs.FLAG_TRUSTED_BENIFICIARIES.getValue());
        add(
                IngConstants.Session.ValuePairs.FLAG_THIRD_PARTY.getKey(),
                IngConstants.Session.ValuePairs.FLAG_THIRD_PARTY.getValue());
        add(
                IngConstants.Session.ValuePairs.FLAG_SIGN_BY_TWO.getKey(),
                IngConstants.Session.ValuePairs.FLAG_SIGN_BY_TWO.getValue());
        add(IngConstants.Session.PROFILE_NAME, aggregator);
        add(IngConstants.Session.DEVICE_NAME, aggregator);
        add(
                IngConstants.Session.ValuePairs.PROFILE_RENEWAL.getKey(),
                IngConstants.Session.ValuePairs.PROFILE_RENEWAL.getValue());
        add(
                IngConstants.Session.ValuePairs.DEVICE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DEVICE_TYPE.getValue());
        add(IngConstants.Storage.DEVICE_ID, deviceId);
        add(IngConstants.Session.MAC_ADDRESS, deviceId);
        add(
                IngConstants.Session.ValuePairs.DSE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }
}
