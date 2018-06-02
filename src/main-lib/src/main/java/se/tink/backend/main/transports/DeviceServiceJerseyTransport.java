package se.tink.backend.main.transports;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.modelmapper.ModelMapper;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.api.DeviceService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.Device;
import se.tink.backend.core.DeviceConfiguration;
import se.tink.backend.core.DeviceConfigurationDto;
import se.tink.backend.core.DeviceOrigin;
import se.tink.backend.core.DeviceOriginDto;
import se.tink.backend.main.controllers.DeviceServiceController;
import se.tink.backend.rpc.DeviceListResponse;
import se.tink.backend.rpc.SetOriginCommand;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.utils.ApiTag;

@Path("/api/v1/devices")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Api(value = ApiTag.DEVICE_SERVICE, description = "This service handles Devices and their configurations, APN and GCM notification tokens.")
public class DeviceServiceJerseyTransport implements DeviceService {

    private final DeviceServiceController deviceServiceController;

    @Inject
    public DeviceServiceJerseyTransport(DeviceServiceController deviceServiceController) {
        this.deviceServiceController = deviceServiceController;
    }

    @PUT
    @Path("{deviceToken}")
    @TeamOwnership(Team.PFM)
    @ApiOperation(
            value = "Update a device",
            notes = "Updates a device for the authenticated user. If the same notificationToken would exist on some another user, it will be removed from that user. If the deviceToken is registered already with a different push token, it will be updated to the latest one.",
            tags = { ApiTag.DEVICE_SERVICE, ApiTag.HIDE }
    )
    @Override
    public void updateDevice(
            @Authenticated AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The (persistent) deviceToken", required = true) @PathParam("deviceToken") String deviceToken,
            @ApiParam(value = "The token and some meta data around it", required = true) Device device) {
        deviceServiceController.updateDevice(authenticatedUser.getUser().getId(), deviceToken, device);
    }

    @DELETE
    @Path("{deviceToken}")
    @TeamOwnership(Team.PFM)
    @ApiOperation(
            value = "Delete a device",
            notes = "Deletes the device. Does not require an authenticated user.",
            tags = { ApiTag.DEVICE_SERVICE, ApiTag.HIDE }
    )
    @Override
    public void deleteDevice(
            @Authenticated(required = false) AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The (persistent) deviceToken", required = true) @PathParam("deviceToken") String deviceToken) {
        deviceServiceController.deleteDevice(deviceToken);
    }

    @GET
    @TeamOwnership(Team.PFM)
    @ApiOperation(
            value = "List all devices",
            notes = "Lists all devices.",
            tags = { ApiTag.DEVICE_SERVICE, ApiTag.HIDE }
    )
    @Override
    public DeviceListResponse listDevices(@Authenticated AuthenticatedUser authenticatedUser) {
        DeviceListResponse response = new DeviceListResponse();
        List<Device> devices = deviceServiceController.listDevices(authenticatedUser.getUser().getId());
        response.setDevices(devices);

        return response;
    }

    @GET
    @Path("{deviceToken}/configuration")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    public DeviceConfigurationDto getConfiguration(
            @PathParam("deviceToken") String deviceToken,
            @QueryParam("desiredMarket") String desiredMarket) {

        DeviceConfiguration configuration = deviceServiceController
                .getConfiguration(UUID.fromString(deviceToken), desiredMarket);

        DeviceConfigurationDto deviceConfigurationDto = new DeviceConfigurationDto();
        deviceConfigurationDto.setFlags(configuration.getFeatureFlags());
        deviceConfigurationDto.setMarkets(configuration.getMarkets());

        return deviceConfigurationDto;
    }

    @POST
    @Path("{deviceToken}/origin")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    public void setOrigin(
            @PathParam("deviceToken") String deviceToken,
            DeviceOriginDto deviceOriginDto) {

        ModelMapper modelMapper = new ModelMapper();
        DeviceOrigin deviceOrigin = modelMapper.map(deviceOriginDto, DeviceOrigin.class);

        deviceServiceController.setOrigin(new SetOriginCommand(deviceToken, deviceOrigin));
    }
}
