package se.tink.backend.aggregation.nxgen.http;

import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IntegrationWireMockTestRequestBuilder implements RequestBuilder {

    private RequestBuilder requestBuilder;

    public IntegrationWireMockTestRequestBuilder(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    @Override
    public URL getUrl() {
        return requestBuilder.getUrl();
    }

    @Override
    public RequestBuilder body(Object body) {
        requestBuilder = requestBuilder.body(body);
        return this;
    }

    @Override
    public RequestBuilder body(Object body, MediaType type) {
        requestBuilder = requestBuilder.body(body, type);
        return this;
    }

    @Override
    public RequestBuilder body(Object body, String type) {
        requestBuilder = requestBuilder.body(body, type);
        return this;
    }

    @Override
    public RequestBuilder type(MediaType type) {
        requestBuilder = requestBuilder.type(type);
        return this;
    }

    @Override
    public RequestBuilder type(String type) {
        requestBuilder = requestBuilder.type(type);
        return this;
    }

    @Override
    public RequestBuilder accept(MediaType... types) {
        requestBuilder = requestBuilder.accept(types);
        return this;
    }

    @Override
    public RequestBuilder accept(String... types) {
        requestBuilder = requestBuilder.accept(types);
        return this;
    }

    @Override
    public RequestBuilder acceptLanguage(Locale... locales) {
        requestBuilder = requestBuilder.acceptLanguage(locales);
        return this;
    }

    @Override
    public RequestBuilder acceptLanguage(String... locales) {
        requestBuilder = requestBuilder.acceptLanguage(locales);
        return this;
    }

    @Override
    public RequestBuilder cookie(String name, String value) {
        requestBuilder = requestBuilder.cookie(name, value);
        return this;
    }

    @Override
    public RequestBuilder cookie(Cookie cookie) {
        requestBuilder = requestBuilder.cookie(cookie);
        return this;
    }

    @Override
    public RequestBuilder header(String name, Object value) {
        // AAP-381: Since Wiremock does not support HTTP2 pseudo headers
        // we convert them to regular headers
        if (name.startsWith(":")) {
            name = name.substring(1);
        }
        requestBuilder = requestBuilder.header(name, value);
        return this;
    }

    @Override
    public RequestBuilder header(HeaderEnum header) {
        // AAP-381: Since Wiremock does not support HTTP2 pseudo headers
        // we convert them to regular headers
        if (header.getKey().startsWith(":")) {
            requestBuilder = requestBuilder.header(header.getKey().substring(1), header.getValue());
        } else {
            requestBuilder = requestBuilder.header(header);
        }
        return this;
    }

    @Override
    public RequestBuilder headers(Map<String, Object> map) {
        // AAP-381: Since Wiremock does not support HTTP2 pseudo headers
        // we convert them to regular headers
        map.entrySet().stream().forEach(entry -> this.header(entry.getKey(), entry.getValue()));
        return this;
    }

    @Override
    public RequestBuilder headers(MultivaluedMap<String, Object> map) {
        // AAP-381: Since Wiremock does not support HTTP2 pseudo headers
        // we convert them to regular headers
        map.entrySet().stream().forEach(entry -> this.header(entry.getKey(), entry.getValue()));
        return this;
    }

    @Override
    public RequestBuilder queryParam(String key, String value) {
        requestBuilder = requestBuilder.queryParam(key, value);
        return this;
    }

    @Override
    public RequestBuilder queryParamRaw(String key, String value) {
        requestBuilder = requestBuilder.queryParamRaw(key, value);
        return this;
    }

    @Override
    public RequestBuilder queryParams(Map<String, String> queryParams) {
        requestBuilder = requestBuilder.queryParams(queryParams);
        return this;
    }

    @Override
    public RequestBuilder queryParams(MultivaluedMap<String, String> queryParams) {
        requestBuilder = requestBuilder.queryParams(queryParams);
        return this;
    }

    @Override
    public RequestBuilder overrideHeader(String name, Object value) {
        // AAP-381: Since Wiremock does not support HTTP2 pseudo headers
        // we convert them to regular headers
        if (name.startsWith(":")) {
            name = name.substring(1);
        }
        requestBuilder = requestBuilder.overrideHeader(name, value);
        return this;
    }

    @Override
    public RequestBuilder addBasicAuth(String username, String password) {
        requestBuilder = requestBuilder.addBasicAuth(username, password);
        return this;
    }

    @Override
    public RequestBuilder addBasicAuth(String username) {
        requestBuilder = requestBuilder.addBasicAuth(username);
        return this;
    }

    @Override
    public RequestBuilder addBearerToken(OAuth2Token token) {
        requestBuilder = requestBuilder.addBearerToken(token);
        return this;
    }

    @Override
    public RequestBuilder removeAggregatorHeader() {
        requestBuilder = requestBuilder.removeAggregatorHeader();
        return this;
    }

    @Override
    public HttpRequest build(HttpMethod method) {
        return requestBuilder.build(method);
    }

    @Override
    public void cutFilterTail() {
        requestBuilder.cutFilterTail();
    }

    @Override
    public RequestBuilder addFilter(Filter filter) {
        requestBuilder = requestBuilder.addFilter(filter);
        return this;
    }

    @Override
    public RequestBuilder removeFilter(Filter filter) {
        requestBuilder = requestBuilder.removeFilter(filter);
        return this;
    }

    @Override
    public boolean isFilterPresent(Filter filter) {
        return requestBuilder.isFilterPresent(filter);
    }

    @Override
    public HttpResponse head() throws HttpClientException {
        return requestBuilder.head();
    }

    @Override
    public <T> T options(Class<T> c) throws HttpResponseException, HttpClientException {
        return requestBuilder.options(c);
    }

    @Override
    public <T> T get(Class<T> c) throws HttpResponseException, HttpClientException {
        return requestBuilder.get(c);
    }

    @Override
    public void put() throws HttpResponseException, HttpClientException {
        requestBuilder.put();
    }

    @Override
    public void put(Object body) throws HttpResponseException, HttpClientException {
        requestBuilder.put(body);
    }

    @Override
    public <T> T put(Class<T> c) throws HttpResponseException, HttpClientException {
        return requestBuilder.put(c);
    }

    @Override
    public <T> T put(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return requestBuilder.put(c, body);
    }

    @Override
    public void patch() throws HttpResponseException, HttpClientException {
        requestBuilder.patch();
    }

    @Override
    public void patch(Object body) throws HttpResponseException, HttpClientException {
        requestBuilder.patch(body);
    }

    @Override
    public <T> T patch(Class<T> c) throws HttpResponseException, HttpClientException {
        return requestBuilder.patch(c);
    }

    @Override
    public <T> T patch(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return requestBuilder.patch(c, body);
    }

    @Override
    public void post() throws HttpResponseException, HttpClientException {
        requestBuilder.post();
    }

    @Override
    public void post(Object body) throws HttpResponseException, HttpClientException {
        requestBuilder.post(body);
    }

    @Override
    public <T> T post(Class<T> c) throws HttpResponseException, HttpClientException {
        return requestBuilder.post(c);
    }

    @Override
    public <T> T post(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return requestBuilder.post(c, body);
    }

    @Override
    public void delete() throws HttpResponseException, HttpClientException {
        requestBuilder.delete();
    }

    @Override
    public void delete(Object body) throws HttpResponseException, HttpClientException {
        requestBuilder.delete(body);
    }

    @Override
    public <T> T delete(Class<T> c) throws HttpResponseException, HttpClientException {
        return requestBuilder.delete(c);
    }

    @Override
    public <T> T delete(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return requestBuilder.delete(c, body);
    }

    @Override
    public void method(HttpMethod method) throws HttpResponseException, HttpClientException {
        requestBuilder.method(method);
    }

    @Override
    public void method(HttpMethod method, Object body) {
        requestBuilder.method(method, body);
    }

    @Override
    public <T> T method(HttpMethod method, Class<T> c) {
        return requestBuilder.method(method, c);
    }

    @Override
    public <T> T method(HttpMethod method, Class<T> c, Object body)
            throws HttpResponseException, HttpClientException {
        return requestBuilder.method(method, c, body);
    }
}
