package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.springframework.http.HttpHeaders;
import se.tink.backend.aggregation.agents.common.ConnectivityRequest;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurRequestHeaderFactory;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr.PasswordVirtualKeyboardOcr;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr.VirtualKeyboardImageParameters;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.connectivity.errors.ConnectivityErrorDetails;

@AllArgsConstructor
public class LoginRequest implements ConnectivityRequest<String> {

    private static final String URL_PATH =
            "/NASApp/BesaideNet2/Gestor?PORTAL_CON_DCT=SI&PRESTACION=login&FUNCION=directoportalImage&ACCION=control";
    public static final String FROM_INDIVIDUALS =
            "https://portal.cajasur.es/cs/Satellite/cajasur/es/particulares-0";

    private final String authUrlDomain;
    private final PasswordVirtualKeyboardOcr virtualKeyboardOcr;
    private final LoginRequestParams params;

    @Override
    public RequestBuilder withHeaders(RequestBuilder requestBuilder) {
        return requestBuilder
                .headers(CajasurRequestHeaderFactory.createBasicHeaders())
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(HttpHeaders.REFERER, FROM_INDIVIDUALS);
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(prepareLoginRequestBody(params));
    }

    @Override
    public RequestBuilder withUrl(TinkHttpClient httpClient) {
        return httpClient.request(new URL(authUrlDomain + URL_PATH));
    }

    @Override
    public String execute(RequestBuilder requestBuilder) {
        return requestBuilder.post(String.class);
    }

    private String prepareLoginRequestBody(LoginRequestParams params) {
        String keyboardedPassword =
                virtualKeyboardOcr.getNumbersSequenceFromImage(
                        params.getPasswordVirtualKeyboardImage(),
                        params.getPassword(),
                        VirtualKeyboardImageParameters.createEnterpriseConfiguration());

        String valueDataLogon =
                encriptarDataLogon(
                        params.getUsername(),
                        keyboardedPassword,
                        params.getObfuscatedLoginJavaScript());
        try {
            return String.format(
                    "idioma=ES&password=%s&tecladoVirtual=SI&usuarioInsertado=%s&usuarioSinFormatear=%s&activador=MP&sitioWeb=&destino=&tipoacceso=&idSegmento=%s&DATA_LOGON_PORTAL=%s",
                    keyboardedPassword,
                    params.getUsername(),
                    params.getUsername(),
                    params.getSegmentId(),
                    URLEncoder.encode(valueDataLogon, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new ConnectivityException(
                            ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR)
                    .withCause(e);
        }
    }

    private String encriptarDataLogon(
            String username, String pwd3, String obfuscatedLoginJavaScript) {
        JavaScriptContext jsContext = createJavaScriptContext(obfuscatedLoginJavaScript);
        String name = "encriptarDataLogon";
        String[] arg = {username, pwd3, "false"};

        Object fobj = jsContext.getScope().get(name, jsContext.getScope());
        if (fobj instanceof Function) {
            Function func = (Function) fobj;
            try {
                Object result =
                        func.call(
                                jsContext.getCx(), jsContext.getScope(), jsContext.getScope(), arg);
                return Context.toString(result);
            } catch (JavaScriptException javaScriptException) {
                throw new IllegalStateException(
                        "Error de sintaxis interpretando JScript", javaScriptException);
            }
        }
        return "undefine";
    }

    private JavaScriptContext createJavaScriptContext(String obfuscatedLoginJavaScript) {
        String jsString =
                String.join(
                        "\n",
                        JavaScriptResourceLoader.loadEnvRhinoJs(),
                        JavaScriptResourceLoader.loadPbkdf2Js(),
                        JavaScriptResourceLoader.loadAesJs(),
                        JavaScriptResourceLoader.loadAesUtilsJs(),
                        obfuscatedLoginJavaScript);

        Context cx = Context.enter();
        Scriptable scope = cx.initStandardObjects(null);
        cx.setOptimizationLevel(-1);
        cx.evaluateString(scope, jsString, "<cmd>", 1, (Object) null);

        return new JavaScriptContext(cx, scope);
    }
}
