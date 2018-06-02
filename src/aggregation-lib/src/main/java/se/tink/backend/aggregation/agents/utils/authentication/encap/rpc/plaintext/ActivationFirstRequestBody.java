package se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapConstants;

public class ActivationFirstRequestBody extends MultivaluedMapImpl {

    public ActivationFirstRequestBody(String deviceHash, String deviceUUID, String applicationVersion,
            String encapAPIVerison ) {
        add("applicationId", EncapConstants.MessageInformation.APPLICATION_ID);
        add("device.DeviceHash", deviceHash);
        add("deviceModel", EncapConstants.DeviceInformation.MODEL);
        add("device.DeviceName", EncapConstants.DeviceInformation.NAME);
        add("device.DeviceUUID", deviceUUID);
        add("device.IsRootAvailable", EncapConstants.DeviceInformation.IS_ROOT_AVAILABLE);
        add("device.SystemName", EncapConstants.DeviceInformation.OS_NAME_AND_TYPE);
        add("device.SystemVersion", EncapConstants.DeviceInformation.SYSTEM_VERSION);
        add("device.UserInterfaceIdiom", 
                EncapConstants.DeviceInformation.USER_INTERFACE_IDIOM);
        add("hexAPNToken", EncapConstants.MessageInformation.HEX_APN_TOKEN);
        add("meta.applicationVersion", applicationVersion);
        add("meta.encapAPIVersion", encapAPIVerison);
        add("operation", EncapConstants.MessageInformation.OPERATION_REGISTER);
        add("response.requireToken", EncapConstants.MessageInformation.REQUIRE_TOKEN);
    }
}
