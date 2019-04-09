package se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapConstants;

public class AuthenticationFirstRequestBody extends MultivaluedMapImpl {

    public AuthenticationFirstRequestBody(
            String deviceHash,
            String deviceUUID,
            String applicationVersion,
            String encapAPIVersion,
            String registrationId) {

        add("applicationId", EncapConstants.MessageInformation.APPLICATION_ID);
        add("clientOnly", EncapConstants.MessageInformation.CLIENT_ONLY);
        add("device.DeviceHash", deviceHash);
        add("device.DeviceModel", EncapConstants.DeviceInformation.MODEL);
        add("device.DeviceName", EncapConstants.DeviceInformation.NAME);
        add("device.DeviceUUID", deviceUUID);
        add("device.IsRootAvailable", EncapConstants.DeviceInformation.IS_ROOT_AVAILABLE);
        add("device.SystemName", EncapConstants.DeviceInformation.OS_NAME_AND_TYPE);
        add("device.SystemVersion", EncapConstants.DeviceInformation.SYSTEM_VERSION);
        add("device.UserInterfaceIdiom", EncapConstants.DeviceInformation.USER_INTERFACE_IDIOM);
        add("meta.applicationVersion", applicationVersion);
        add("meta.encapAPIVersion", encapAPIVersion);
        add("operation", EncapConstants.MessageInformation.OPERATION_IDENTIFY);
        add("registrationId", registrationId);
        add("response.requireToken", EncapConstants.MessageInformation.REQUIRE_TOKEN);
    }
}
