package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.AppEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.CustomEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.DeviceEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.JoseHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.JwtPayloadEntity;

public final class AuthenticateJwt implements Jwt {

    private final String token;
    private final String certificate;
    private final String moduleName;

    public AuthenticateJwt(final String token, final String certificate, final String moduleName) {
        this.token = token;
        this.certificate = certificate;
        this.moduleName = moduleName;
    }

    @Override
    public JoseHeaderEntity getJoseHeader() {
        JoseHeaderEntity entity = new JoseHeaderEntity();
        entity.setAlg(WLConstants.ALG);
        entity.setX5c(this.certificate);

        return entity;
    }

    @Override
    public JwtPayloadEntity getPayload() {
        CustomEntity custom = new CustomEntity();

        AppEntity app = new AppEntity();
        app.setId(moduleName);
        app.setVersion(WLConstants.WL_APP_VERSION);

        DeviceEntity device = new DeviceEntity();
        device.setEnvironment(WLConstants.ENVIRONMENT);
        device.setId(WLConstants.DEVICE_ID); // Same as x-wl-device-id found in headers
        device.setModel(WLConstants.MODEL);
        device.setOs(WLConstants.OS);

        JwtPayloadEntity id = new JwtPayloadEntity();
        id.setApp(app);
        id.setCustom(custom);
        id.setDevice(device);
        id.setToken(token);

        return id;
    }

}
