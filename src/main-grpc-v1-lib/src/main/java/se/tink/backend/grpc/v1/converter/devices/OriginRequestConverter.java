package se.tink.backend.grpc.v1.converter.devices;

import se.tink.backend.core.AppsFlyerDeviceOrigin;
import se.tink.backend.core.DeviceOrigin;
import se.tink.backend.core.FacebookDeviceOrigin;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.rpc.SetOriginCommand;
import se.tink.grpc.v1.rpc.SetOriginRequest;

public class OriginRequestConverter implements Converter<SetOriginRequest, SetOriginCommand> {
    @Override
    public SetOriginCommand convertFrom(SetOriginRequest input) {

        DeviceOrigin origin = new DeviceOrigin();
        origin.setOrganic(input.getOrganic());
        origin.setServiceName(input.getServiceName());
        origin.setExternalServiceId(input.getExternalServiceId());
        origin.setMediaSource(input.getMediaSource());
        origin.setCampaign(input.getCampaign());
        origin.setAgency(input.getAgency());
        origin.setClickTime(input.getClickTime());
        origin.setInstallTime(input.getInstallTime());

        AppsFlyerDeviceOrigin appsFlyerDeviceOrigin = new AppsFlyerDeviceOrigin();
        appsFlyerDeviceOrigin.setExtraParam1(input.getAppsFlyer().getExtraParam1());
        appsFlyerDeviceOrigin.setExtraParam2(input.getAppsFlyer().getExtraParam2());
        appsFlyerDeviceOrigin.setExtraParam3(input.getAppsFlyer().getExtraParam3());
        appsFlyerDeviceOrigin.setExtraParam4(input.getAppsFlyer().getExtraParam4());
        appsFlyerDeviceOrigin.setExtraParam5(input.getAppsFlyer().getExtraParam5());
        origin.setAppsFlyer(appsFlyerDeviceOrigin);

        FacebookDeviceOrigin facebookDeviceOrigin = new FacebookDeviceOrigin();
        facebookDeviceOrigin.setCampaignId(input.getFacebook().getCampaignId());
        facebookDeviceOrigin.setAdGroupId(input.getFacebook().getAdGroupId());
        facebookDeviceOrigin.setAdGroupName(input.getFacebook().getAdGroupName());
        facebookDeviceOrigin.setAdSetId(input.getFacebook().getAdSetId());
        facebookDeviceOrigin.setAdSetName(input.getFacebook().getAdSetName());
        facebookDeviceOrigin.setAdId(input.getFacebook().getAdId());
        origin.setFacebook(facebookDeviceOrigin);
        String deviceId = input.getDeviceId();
        return new SetOriginCommand(deviceId, origin);
    }
}
