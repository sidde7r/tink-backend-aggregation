package se.tink.backend.aggregation.nxgen.http.legacy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.legacy.entities.JwtBodyEntity;
import se.tink.backend.aggregation.nxgen.http.legacy.entities.JwtHeaderEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

/*
   This HttpRequestExecutor is only necessary because of bugs in the underlying libraries (jersey and apache).
   Adding cookies to single requests will lead to multiple `Cookie` headers because apache adds cookies from
   the internal cookieStore (which is populated by Set-Cookie directives).

   The work-around is to merge all `Cookie` headers into one.

   (This class also removes the header `Cookie2` which is added by apache).
*/
public class TinkApacheHttpRequestExecutor extends HttpRequestExecutor {
    private static final String RESOLVED_APPID_PLACE_HOLDER = "RESOLVEDAPPIDPLACEHOLDER";
    private static final Logger log = LoggerFactory.getLogger(TinkApacheHttpRequestExecutor.class);
    private static final String SIGNATURE_HEADER_KEY = "X-Signature";
    private static final String SIGNATURE_INFO_HEADER_KEY = "X-Signature-Info";
    private static final String EIDAS_CLUSTER_ID_HEADER = "X-Tink-QWAC-ClusterId";
    private static final String EIDAS_APPID_HEADER = "X-Tink-QWAC-AppId";
    // TODO: Make a lib for these shared headers
    private static final String EIDAS_CERTID_HEADER = "X-Tink-QWAC-CertId";
    private static final String EIDAS_PROXY_REQUESTER = "X-Tink-Debug-ProxyRequester";

    private static final ImmutableSet<String> ALLOWED_CLUSTERIDS_FOR_QSEALCSIGN =
            ImmutableSet.of(
                    "oxford-production",
                    "kirkby-staging",
                    "kirkby-production",
                    "farnham-staging",
                    "neston-production");

    // All isAggregator=TRUE appIds
    private static final ImmutableSet<String> DISALLOWED_APPIDS_FOR_QSEALCSIGN =
            ImmutableSet.of(
                    "22d854bfe1a2486c8a0173d330cf6322",
                    "0a0d0ebe75684dc59dce4739034f5627",
                    "22ceb092b6604a658ace9d131b6604dc",
                    "2c17d4f1ecb446b8b85808dc73a9f63f",
                    "4c31b94ca11544dca5fbc34d10694118",
                    "50e1d054d6dc467cadde44d2d18da2cc",
                    "59b2c8799ada48c381637ebc96d455bc",
                    "5ed32ef6a7314a658b1c57a5431f2978",
                    "68a6db2e54a94f65b14ae25128dd9e7d",
                    "6bb8cc19b3be4f329800caf45ce96c92",
                    "6de5777031f243ca8d2229bd30e5fe5d",
                    "726c95011c994aaf9e3a9c3ca25911b0",
                    "86da855bcdb44dacb612195cdbd761a3",
                    "98e9f29627fb439b9398d13a0c2ba4f0",
                    "9c633b78113e412b94dd94969843a1f2",
                    "a1f9f90779924e0bbae96e0b6bf5cb78",
                    "ad8ffc7a304341c6b64930ab34b20670",
                    "c5bd3bbf9e4c43f5805eaf12e5d8ce35",
                    "c63d0e2305b2440ca7fc039665a69d5a",
                    "cd7eda70a19143478b8844dcda83038d",
                    "f79f37b917784fcfbf4150295b710e88",
                    "36cbaccd848148449f34c2b4305db932",
                    "0ff7cba8c4094e29b94ba6812ad81d96",
                    "db6edf80de054c7aa14c6fe8aa26e8c8",
                    "a7d4b863a7294e1cb1308cb0ec233e20",
                    "063c613bd71345a9a89f380831197ab6",
                    "0ef63a049fd346e2b96e3e824f4974b8",
                    "1ce03bb5c2a74da9a78c79a0deb4b66b",
                    "71ca273fae4b4e4eb78f95324d609955",
                    "11e69a6b69a14f82809f7d5113f91f7e",
                    "8a9bff73d3a940f9909e4e1ea46e1550",
                    "b8e09493724c454fa5ca5ec6663f37a3",
                    "772ead2c31694115915d0cc505213a02",
                    "6efdcfbed9644b97aeeaf8b8c261d96c",
                    "61584d2fd46d435583c3826c468f7f43",
                    "cee545c0bbc843b3861b959d4b884d42",
                    "655e700ff6a74a618484ee28bca05d95",
                    "755100024c524107950b31447b418391",
                    "77973c9a15c04098b819754fd1c40f52",
                    "f3420381d7d846fea5974eec0568932d",
                    "18e699b8b3fc47489fcc26b55345d886",
                    "1d23aff0566c41418cfd4973aae2bb3b",
                    "e1af83a8cc174f309d3f8b380988aafc",
                    "dc9bfc8e43684607b660d24beadce1c3",
                    "bf766d3aba0243b688df44296ed2d274",
                    "880e2dcc110d44fca65abc9a55b2b3db",
                    "e2405849ab174405997e8aab3e79a9a0",
                    "6865e34438844116a7d8e6f7cd121297",
                    "f89d31e215b44870983252ee5a28598e",
                    "5bdff90ba5f54b578b6bcd4a93f68f54",
                    "2fe7f5bb1b164192b1d450a16aeac2f2",
                    "d5cb400b4c364ea786cf7c93e91d31ad",
                    "c8abc7841d524ed0bb5744bfbcda1cfd",
                    "3a449a008cc14dc0a888ab4cc86a6645",
                    "c9ffac55a2e943d88c22bf0bd6972986",
                    "6c22453a598644ee98626621332d867f",
                    "b3d1a13733b0426aba8ec45057f3e182",
                    "45d37e3b1b8f4a46b5a031fcb6745004",
                    "5f558ebb937d4082925588f1ba656857",
                    "e74b7faf76fc4bf89e03711d8ef4da0b",
                    "d4094154b219410688d457f727da8ca4",
                    "aab8687a4584488480da41221311b1f1",
                    "86d21fa207e045429c720531d378a9e0",
                    "986a9e244c26451085edd2df33a68e59",
                    "12eb2f2145e74659819ab75417327cdd",
                    "421c175de3da403180094d0ee2811aa0",
                    "99f90f6daa6944938ce56174e8ea8829",
                    "6befb011567d428e919fba7faaaada83",
                    "caaba0587c504db9a2bcfa1d150e5bf7",
                    "ce5c6bd43db84d84b7bf62cd806d9c69",
                    "5ca6cc5357b848788ca51318d9c4c5fc",
                    "dd62847c2e4c4da0824a61dd364fabd0",
                    "10d3037a0bf9415fa6b96a1e9dce3505",
                    "fe19598246c34d51ae3dc36ec5ef45bc",
                    "9083650d1cb24035bc2cee010f4ae8a6",
                    "c0f81368e2c3422c8abac1e60f029765",
                    "59e88a52d5414cd991f1fdadf080ec16",
                    "5f0ef90db2614a00a8b948fa15af6777",
                    "13a017b7cdbd486ba75a124ce842cfe2",
                    "309be78f171a49dd9423dcd5b0a33810",
                    "8d3e7589fcca45c3b9b6eafab74fb2eb",
                    "59370bc862c54649b2cabf05f9afaac3",
                    "d091a999367647a09332733dedb01762",
                    "59c9ca2b48f2491a9931f2c6f55257b5",
                    "263cce748b2141ee9353d9dcbeacae69",
                    "dde7f7ce71f445499a1fe1158ed638d0",
                    "8f4384a2a64946b58aafe4d7ae483139",
                    "dea9dba17ec94bddb48ed8d9120a3f05",
                    "f1866cd42f0d44aebe454cdeffe9598f",
                    "18763028d30c4a11a5cdf32a3b09abb4",
                    "96dbd3f6eb1e4974ad6f0ec14becc581",
                    "c7e816a0eb5747f5bb49126cf5f2678a",
                    "2ab6e0f72b4d457a8c925539dcdfcac5",
                    "72979bd16d3e42c0b302cff96dd69567",
                    "df01cee2953c4d8e8296b2ef94d241f0",
                    "e8ea892541c640d290bce24d77315168",
                    "e74a9bdf259f416abb8f8f9f61e5bae3",
                    "7e9570c5369640e69ae9ab79c3e91b63",
                    "6f5db85d03a24ccd8a1fe18215283280",
                    "422133ade348446d98cdab6063fea92b",
                    "a28f2fe3cf94417193e8892cc380e58a",
                    "d2f2d32c648c426e85b5ceae1b8b78a2",
                    "59f4afd2be3345e2a710819459ed35f3",
                    "f26ff853c20e41dd9b6ccd1f4d49f961",
                    "3808a4ff697844c69fb0e28c70fbde04",
                    "d8e7e1e343664bfe84250a8bbfbc6054",
                    "496629a2790345d68f03f19707032dda",
                    "213a3d0ec0ac4f47b2f103ecc80c7f8f",
                    "36cd54bfdbb04d58907f4fc65cfbcc71",
                    "85a85d5f863f4e27a13f251379f2f3e5",
                    "b66c08d2a35a409ba08eb6a0afddc19d",
                    "88f78b12cef745ec84b017e8d285338d",
                    "dca52d8fb3644361bb200067e47685b0",
                    "2b7c112fcf6c49988a4b6d9f492d9a93",
                    "948b9d03dd9b4615880dfd26a987d17d",
                    "b806f529d3764f99a367d548e2435eef",
                    "070c3e06a3fa4b8e96ee715b1102034c",
                    "77ba5e53ed2b44c4aa280f90a3a8aae9",
                    "24d5b54e115c4421b7f8c932c27c2c48",
                    "9257bff04aea4879b624c540a2273e76",
                    "f598141a8d0a4fdaa67dd47de875fa6c",
                    "c5f5a6c6fa504c7b887aa7c93ff8d68a",
                    "160eb0232596469abec43351eacb712b",
                    "8ad61c6b125f4c609ddc10ec98f23f37",
                    "a36014cce10a4ea8b87f45c6845ec6e6",
                    "098449ee14c64710a73be51f30462d3f",
                    "92ee9f245b644fac9335facc0efd7fa1",
                    "72a330e7234f49fea66b295f020c7112",
                    "97ac28d393e04eff832b4c78bbd2b3d6",
                    "a2002331faa14c3fbc1e3b7b765b2b80",
                    "9d795ebbc9464b9ba041946932622218",
                    "b337f009dcf34dc4b4b0b8578370b99a",
                    "9eb72ab214a0407ebebbc521531869c3",
                    "0983c0ef2be64d1e9cfe427a1a555689",
                    "fffbe8fe1fcf4e958f34d7c507d2d981",
                    "153c35a2405a4900a032e07d6c66e6f0",
                    "ad2d40e5cab4474188e9c935d162cccb",
                    "e81d000bd7ce40e5b889ccd75dc749e6",
                    "8d9a3f02e6fa42138e1ba73cb4f6fcb3",
                    "ba767cb0db454357bc36e9eac061ee28",
                    "b5cfcae6a25e413a9bcdb15bdfb02604",
                    "1cd9aa58ff1b468a9c0fee74fed1334a",
                    "b41758bd78494b948dc6f1b078aa0ee3",
                    "0616fef40b6c46f48cbc6eb00d35092f",
                    "2f780ee271f047ad83cc4b811f7fab54",
                    "98d926e9f2c14f40aef525c5b6e57f3c",
                    "2c5bc4b87fd5475bb0571bc04ad8b77c",
                    "3a7c7e95a4074aafa6c398547c318147",
                    "0eb2c951081e44b188ba522ccf7985db",
                    "141e7673998e4960b20a6297e4a60402",
                    "1ab74aef6d8f4205b6649b1e8b16e747",
                    "252806b50b2c4b57ae71a2dba6b7bd2b",
                    "3210521c36004ae989ac80f9788c8f54",
                    "4fc361b9b14f4dfaaa499f551f687f78",
                    "5339e2430b064bc1a74fb014ece3d0e9",
                    "57663ae04650468dba5a6ba122ee3cef",
                    "60af1c4cef2d47c58bdbc7b420992740",
                    "61f002506d46439489a18a432c34d0b4",
                    "62c17407b983498d8c1577d0123f6e23",
                    "7ce20e26ce72483b8ebe0fe70491a59e",
                    "8124ad2d8a2c4aed8470135f36d95979",
                    "8134a9157896417c9f3ad33ef91c2ddb",
                    "8400cbe0a9774f3eb7a9f934b3867d1c",
                    "8ef05a90229340c6b70a1a826c0721e1",
                    "a4bb0433ec594c8cbac8397138cdbdcb",
                    "a637d239f473454f9b5776709e2be54e",
                    "b219f0b03ee24d86a808e1bde08bd79c",
                    "bdf5497ce5bb4a95a9b91f3b22289e45",
                    "c0bf2b0ef00e4c9ea31c0ea8ae18512f",
                    "c37144b3aaa04c47b579334737af3e73",
                    "caa029d11bfe4f8cb21f055dfd63cf83",
                    "d6cc626a99014548bdcf23ed89c3ee9e",
                    "eb750ea914794abb8e2a70977b936f35",
                    "cdadab612b3246d3965aa9247482824e");

    private DefaultRequestLoggingAdapter requestLoggingAdapter;

    private SignatureKeyPair signatureKeyPair;
    private Algorithm algorithm;
    private boolean shouldAddRequestSignature = true;

    private String proxyUsername;
    private String proxyPassword;

    private boolean shouldUseEidasProxy = false;
    private EidasIdentity eidasIdentity;
    private EidasProxyConfiguration eidasProxyConfiguration;

    public void setEidasProxyConfiguration(EidasProxyConfiguration eidasProxyConfiguration) {
        this.eidasProxyConfiguration = eidasProxyConfiguration;
    }

    public void setEidasIdentity(EidasIdentity eidasIdentity) {
        this.eidasIdentity = eidasIdentity;
    }

    public TinkApacheHttpRequestExecutor(SignatureKeyPair signatureKeyPair) {
        if (signatureKeyPair == null || signatureKeyPair.getPrivateKey() == null) {
            return;
        }

        this.signatureKeyPair = signatureKeyPair;

        algorithm =
                Algorithm.RSA256(signatureKeyPair.getPublicKey(), signatureKeyPair.getPrivateKey());
    }

    public void setProxyCredentials(String username, String password) {
        this.proxyUsername = username;
        this.proxyPassword = password;
    }

    public void shouldUseEidasProxy() {
        this.shouldUseEidasProxy = true;
    }

    public void setRequestLoggingAdapter(DefaultRequestLoggingAdapter requestLoggingAdapter) {
        this.requestLoggingAdapter = requestLoggingAdapter;
    }

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context)
            throws IOException, HttpException {
        // Remove the default "Cookie2" header that ApacheHttp adds
        request.removeHeaders("Cookie2");
        mergeCookieHeaders(request);

        // Authentication towards the EIDAS proxy is with TLS-MA, we don't need to add an auth
        // header for EIDAS requests.
        if (isHttpProxyRequest(request) && (!shouldUseEidasProxy)) {
            addProxyAuthorizationHeader(request);
        } else if (shouldUseEidasProxy) {
            addEidasProxyHeaders(request);
        } else if (shouldAddRequestSignature) {
            addQsealcOrRequestSignature(request);
        }

        if (!isHttpProxyRequest(request)) {
            log(request);
        }
        return super.execute(request, conn, context);
    }

    private void addEidasProxyHeaders(HttpRequest request) {
        request.addHeader(EIDAS_CLUSTER_ID_HEADER, eidasIdentity.getClusterId());
        request.addHeader(EIDAS_APPID_HEADER, eidasIdentity.getAppId());
        request.addHeader(EIDAS_CERTID_HEADER, eidasIdentity.getCertId());
        request.addHeader(EIDAS_PROXY_REQUESTER, eidasIdentity.getRequester());
    }

    private boolean isHttpProxyRequest(HttpRequest request) {
        return "connect".equalsIgnoreCase(request.getRequestLine().getMethod());
    }

    private void addProxyAuthorizationHeader(HttpRequest request) {
        if (Strings.isNullOrEmpty(proxyUsername) || Strings.isNullOrEmpty(proxyPassword)) {
            return;
        }

        // Note: The apache version we use cannot automatically add the `Proxy-Authorization` via
        // proxy authentication
        // configuration.
        // Remove this code once Apache has been updated to a new version where that functionality
        // works.
        request.addHeader(
                "Proxy-Authorization",
                String.format(
                        "Basic %s",
                        Base64.getUrlEncoder()
                                .encodeToString(
                                        String.format("%s:%s", proxyUsername, proxyPassword)
                                                .getBytes())));
    }

    public void disableSignatureRequestHeader() {
        this.shouldAddRequestSignature = false;
    }

    private void mergeCookieHeaders(HttpRequest request) {
        List<Header> cookieHeaders = Arrays.asList(request.getHeaders("Cookie"));
        if (cookieHeaders.size() <= 1) {
            return;
        }

        // Remove them from the request before adding the merged value
        request.removeHeaders("Cookie");

        String cookieValue =
                cookieHeaders.stream().map(Header::getValue).collect(Collectors.joining("; "));

        request.addHeader("Cookie", cookieValue);
    }

    private void addQsealcOrRequestSignature(HttpRequest request) {
        // For RE request, try to add authentication header with corresponding QSealC cert.
        // * If eidasIdentity is null (for legacy agent), fallback to use self signed cert.
        // * If QSealC cert can't be found or other exceptions, fallback as well and log the
        // error.
        // Roll out this for oxford users now.
        if (eidasIdentity != null && eidasIdentity.getAppId() != null) {
            try {
                if (!DISALLOWED_APPIDS_FOR_QSEALCSIGN.contains(eidasIdentity.getAppId())
                        && ALLOWED_CLUSTERIDS_FOR_QSEALCSIGN.contains(
                                eidasIdentity.getClusterId())) {
                    addQsealcSignatureByGetingWholeJwsToken(request);
                }
            } catch (Exception e) {
                log.warn(
                        "Error occurred in QSealC signing, appId {} certId {} clusterId {}",
                        eidasIdentity.getAppId(),
                        eidasIdentity.getCertId(),
                        eidasIdentity.getClusterId(),
                        e);
                addRequestSignature(request);
            }
        } else {
            addRequestSignature(request);
        }
    }

    private void addQsealcSignatureByGetingWholeJwsToken(HttpRequest request) {
        // TODO: adding the info header once verified
        JwtBodyEntity jwtBody = new JwtBodyEntity();
        RequestLine requestLine = request.getRequestLine();
        jwtBody.setMethod(requestLine.getMethod());
        jwtBody.setUri(requestLine.getUri());
        getHttpHeadersHashAsBase64(request).ifPresent(jwtBody::setHeaders);

        getHttpBodyHashAsBase64(request).ifPresent(jwtBody::setBody);
        jwtBody.setNonce(UUID.randomUUID().toString());
        jwtBody.setIat(OffsetDateTime.now().toEpochSecond());

        String tokenBodyJson = SerializationUtils.serializeToString(jwtBody);
        // TODO, idea is to use appId as keyId and upload cert to corresponding path on CDN
        String tokenHeadJson =
                SerializationUtils.serializeToString(
                        new JwtHeaderEntity(RESOLVED_APPID_PLACE_HOLDER));

        String baseTokenString =
                Base64.getUrlEncoder()
                                .encodeToString(
                                        tokenHeadJson != null
                                                ? tokenHeadJson.getBytes()
                                                : new byte[0])
                        + "."
                        + Base64.getUrlEncoder()
                                .encodeToString(
                                        tokenBodyJson != null
                                                ? tokenBodyJson.getBytes()
                                                : new byte[0]);

        QsealcSigner signer =
                QsealcSignerImpl.build(
                        eidasProxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_JWT_RSA_SHA256,
                        eidasIdentity);
        String jwt = signer.getJWSToken(baseTokenString.getBytes());
        request.addHeader(SIGNATURE_HEADER_KEY, jwt);
    }

    private void addRequestSignature(HttpRequest request) {
        // TODO remove the signatureKeyPair and algorithm

        if (signatureKeyPair == null || algorithm == null) {
            return;
        }

        // This header needs to be added before we fetch the headers to create the signature.
        // Note: This header can be removed if this URL is added to the JWT.
        request.addHeader(
                SIGNATURE_INFO_HEADER_KEY,
                "Visit https://cdn.tink.se/aggregation-signature/how-to-verify.txt for more info.");

        RequestLine requestLine = request.getRequestLine();

        JWTCreator.Builder jwtBuilder =
                JWT.create()
                        .withIssuedAt(new Date())
                        .withClaim("method", requestLine.getMethod())
                        .withClaim("uri", requestLine.getUri());

        // Only add keyId for request where we use signatureKeyPair
        if (signatureKeyPair != null) {
            jwtBuilder.withKeyId(signatureKeyPair.getKeyId());
        }

        getHttpHeadersHashAsBase64(request)
                .ifPresent(hash -> jwtBuilder.withClaim("headers", hash));

        getHttpBodyHashAsBase64(request).ifPresent(hash -> jwtBuilder.withClaim("body", hash));

        request.addHeader(SIGNATURE_HEADER_KEY, jwtBuilder.sign(algorithm));
    }

    private Optional<String> getHttpBodyHashAsBase64(HttpRequest request) {
        if (!(request instanceof HttpEntityEnclosingRequest)) {
            return Optional.empty();
        }

        HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
        if (entity == null) {
            // Handle the case of an empty post request
            return Optional.empty();
        }

        try {
            byte[] bodyBytes = IOUtils.toByteArray(entity.getContent());
            byte[] digest = Hash.sha256(bodyBytes);

            return Optional.of(EncodingUtils.encodeAsBase64String(digest));
        } catch (IOException e) {
            log.error("Could not get the request body from the entity content", e);
            return Optional.empty();
        }
    }

    private Optional<String> getHttpHeadersHashAsBase64(HttpRequest request) {
        Header[] allHeaders = request.getAllHeaders();

        String sortedHeaders =
                Arrays.stream(allHeaders)
                        .filter(Objects::nonNull)
                        .map(header -> String.format("%s: %s", header.getName(), header.getValue()))
                        .sorted(String::compareTo)
                        .collect(Collectors.joining("\n"));

        if (Strings.isNullOrEmpty(sortedHeaders)) {
            return Optional.empty();
        }

        byte[] headerBytes = sortedHeaders.getBytes(StandardCharsets.UTF_8);
        byte[] digest = Hash.sha256(headerBytes);
        return Optional.of(EncodingUtils.encodeAsBase64String(digest));
    }

    /**
     * Request is logged in executor instead of filter to print all outgoing headers
     *
     * @param httpRequest
     */
    private void log(HttpRequest httpRequest) {
        if (requestLoggingAdapter != null) {
            requestLoggingAdapter.logRequest(httpRequest);
        }
    }
}
