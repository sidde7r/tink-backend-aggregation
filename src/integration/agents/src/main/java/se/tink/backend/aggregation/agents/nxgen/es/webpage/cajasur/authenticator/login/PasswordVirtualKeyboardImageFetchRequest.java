package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.common.ConnectivityRequest;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurRequestHeaderFactory;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.connectivity.errors.ConnectivityErrorDetails;

@AllArgsConstructor
public class PasswordVirtualKeyboardImageFetchRequest
        implements ConnectivityRequest<BufferedImage> {

    private static final String URL_PATH =
            "/NASApp/BesaideNet2/Gestor?PRESTACION=login&FUNCION=login&ACCION=directoportalImage&idioma=ES&i=11";

    private final String authUrlDomain;

    @Override
    public RequestBuilder withHeaders(RequestBuilder requestBuilder) {
        return requestBuilder
                .headers(CajasurRequestHeaderFactory.createBasicHeaders())
                .accept("image/png", "image/svg+xml", "image/jxr", "image/*;q=0.8, */*;q=0.5");
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withUrl(TinkHttpClient httpClient) {
        return httpClient.request(new URL(String.format("%s%s", authUrlDomain, URL_PATH)));
    }

    @Override
    public BufferedImage execute(RequestBuilder requestBuilder) {
        byte[] response = requestBuilder.get(byte[].class);
        try {
            return ImageIO.read(new BufferedInputStream(new ByteArrayInputStream(response)));
        } catch (IOException e) {
            throw new ConnectivityException(
                            ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR)
                    .withInternalMessage("Fetching virtual keyboard failed")
                    .withCause(e);
        }
    }
}
