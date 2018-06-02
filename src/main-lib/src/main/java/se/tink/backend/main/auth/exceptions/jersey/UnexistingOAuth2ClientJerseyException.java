package se.tink.backend.main.auth.exceptions.jersey;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UnexistingOAuth2ClientJerseyException extends WebApplicationException {
    public UnexistingOAuth2ClientJerseyException() {
        super(Response
                .status(401)
                .build());
    }
}
