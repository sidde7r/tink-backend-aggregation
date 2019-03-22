package se.tink.backend.nasa.boot;

import com.google.gson.Gson;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import se.tink.backend.nasa.boot.rpc.RefreshInformationRequest;

public class AggregationClient {

    private static final String X_TINK_CLIENT_API_KEY = "X-Tink-Client-Api-Key";
    private static final String CONTENT_TYPE = "Content-type";

    public static HttpResponse refreshInformation(
            String apiClientKey, RefreshInformationRequest request)
            throws IOException, KeyManagementException, NoSuchAlgorithmException {

        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(
                        builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        CloseableHttpClient client = HttpClientBuilder.create().setSSLSocketFactory(sslsf).build();

        // TODO: Fetch stuff from config instead of hardcoded
        HttpPost httpPost = new HttpPost("https://192.168.99.100:31011" + "/aggregation/refresh");

        String jsonRequest = new Gson().toJson(request);
        httpPost.setEntity(new StringEntity(jsonRequest));
        httpPost.setHeader(CONTENT_TYPE, "application/json");
        httpPost.setHeader(X_TINK_CLIENT_API_KEY, apiClientKey);

        CloseableHttpResponse closeableHttpResponse = client.execute(httpPost);
        closeableHttpResponse.close();
        return closeableHttpResponse;
    }
}
