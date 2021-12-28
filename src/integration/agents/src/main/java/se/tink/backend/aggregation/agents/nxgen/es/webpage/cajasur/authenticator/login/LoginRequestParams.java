package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import java.awt.image.BufferedImage;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class LoginRequestParams {

    private String username;
    private String password;
    private String segmentId;
    private String obfuscatedLoginJavaScript;
    private BufferedImage passwordVirtualKeyboardImage;
}
