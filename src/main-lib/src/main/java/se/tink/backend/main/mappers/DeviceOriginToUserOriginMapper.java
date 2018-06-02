package se.tink.backend.main.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import se.tink.backend.core.DeviceOrigin;
import se.tink.backend.core.UserOrigin;

public class DeviceOriginToUserOriginMapper {
    private static final PropertyMap<DeviceOrigin, UserOrigin> PROPERTY_MAP = new PropertyMap<DeviceOrigin, UserOrigin>() {
        @Override
        protected void configure() {
            map().setExtraParam1(source.getAppsFlyer().getExtraParam1());
            map().setExtraParam2(source.getAppsFlyer().getExtraParam2());
            map().setExtraParam3(source.getAppsFlyer().getExtraParam3());
            map().setExtraParam4(source.getAppsFlyer().getExtraParam4());
            map().setExtraParam5(source.getAppsFlyer().getExtraParam5());

            map().setFacebook(source.isFacebook());
            map().setFbCampaignId(source.getFacebook().getCampaignId());
            map().setFbAdSetId(source.getFacebook().getAdSetId());
            map().setFbAdSetName(source.getFacebook().getAdSetName());
            map().setFbAdGroupName(source.getFacebook().getAdGroupName());
            map().setFbAdGroupId(source.getFacebook().getAdGroupId());
            map().setFbAdId(source.getFacebook().getAdId());
        }
    };

    private static ModelMapper getModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(DeviceOriginToUserOriginMapper.PROPERTY_MAP);
        return modelMapper;
    }

    public static UserOrigin map(DeviceOrigin deviceOrigin) {
        return getModelMapper().map(deviceOrigin, UserOrigin.class);
    }
}
