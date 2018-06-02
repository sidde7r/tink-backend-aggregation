package se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapConstants;

public class ActivationSecondRequestBody extends MultivaluedMapImpl {

    public ActivationSecondRequestBody(String b64AuthenticationKey, String b64AuthenticationKeyWithoutPin,
            String b64ChallengeResponse, String b64ChallengeResponseWithoutPin, String deviceHash, String deviceUUID,
            String applicationVersion, String encapAPIVersion, String registrationId, String saltHash) {

        add("applicationId", EncapConstants.MessageInformation.APPLICATION_ID);
        add("b64AuthenticationKey", b64AuthenticationKey);
        add("b64AuthenticationKeyWithoutPin", b64AuthenticationKeyWithoutPin);
        add("b64ChallengeResponse", b64ChallengeResponse);
        add("b64ChallengeResponseWithoutPin", b64ChallengeResponseWithoutPin);
        add("device.DeviceHash", deviceHash);
        add("device.DeviceModel", EncapConstants.DeviceInformation.MODEL);
        add("device.DeviceName", EncapConstants.DeviceInformation.NAME);
        add("device.DeviceUUID", deviceUUID);
        add("device.IsRootAvailable", EncapConstants.DeviceInformation.IS_ROOT_AVAILABLE);
        add("device.SystemName", EncapConstants.DeviceInformation.OS_NAME_AND_TYPE);
        add("device.SystemVersion", EncapConstants.DeviceInformation.SYSTEM_VERSION);
        add("device.UserInterfaceIdiom", 
                EncapConstants.DeviceInformation.USER_INTERFACE_IDIOM);
        add("meta.applicationVersion", applicationVersion);
        add("meta.encapAPIVersion", encapAPIVersion);
        add("operation", EncapConstants.MessageInformation.OPERATION_ACTIVATE);
        add("registrationId", registrationId);
        add("saltHash", saltHash);
    }
}
