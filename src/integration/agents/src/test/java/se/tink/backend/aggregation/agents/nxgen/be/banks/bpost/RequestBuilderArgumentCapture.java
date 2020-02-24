package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost;

import java.util.HashMap;
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
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class RequestBuilderArgumentCapture implements RequestBuilder {

    private Map<String, Object> headers = new HashMap<>();

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public HttpResponse head() throws HttpClientException {
        return null;
    }

    @Override
    public <T> T options(Class<T> c) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public <T> T get(Class<T> c) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public void put() throws HttpResponseException, HttpClientException {}

    @Override
    public void put(Object body) throws HttpResponseException, HttpClientException {}

    @Override
    public <T> T put(Class<T> c) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public <T> T put(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public void patch() throws HttpResponseException, HttpClientException {}

    @Override
    public void patch(Object body) throws HttpResponseException, HttpClientException {}

    @Override
    public <T> T patch(Class<T> c) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public <T> T patch(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public void post() throws HttpResponseException, HttpClientException {}

    @Override
    public void post(Object body) throws HttpResponseException, HttpClientException {}

    @Override
    public <T> T post(Class<T> c) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public <T> T post(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public void delete() throws HttpResponseException, HttpClientException {}

    @Override
    public void delete(Object body) throws HttpResponseException, HttpClientException {}

    @Override
    public <T> T delete(Class<T> c) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public <T> T delete(Class<T> c, Object body) throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public void method(HttpMethod method) throws HttpResponseException, HttpClientException {}

    @Override
    public void method(HttpMethod method, Object body) {}

    @Override
    public <T> T method(HttpMethod method, Class<T> c) {
        return null;
    }

    @Override
    public <T> T method(HttpMethod method, Class<T> c, Object body)
            throws HttpResponseException, HttpClientException {
        return null;
    }

    @Override
    public RequestBuilder body(Object body) {
        return null;
    }

    @Override
    public RequestBuilder body(Object body, MediaType type) {
        return null;
    }

    @Override
    public RequestBuilder body(Object body, String type) {
        return null;
    }

    @Override
    public RequestBuilder type(MediaType type) {
        return null;
    }

    @Override
    public RequestBuilder type(String type) {
        return null;
    }

    @Override
    public RequestBuilder accept(MediaType... types) {
        return null;
    }

    @Override
    public RequestBuilder accept(String... types) {
        return null;
    }

    @Override
    public RequestBuilder acceptLanguage(Locale... locales) {
        return null;
    }

    @Override
    public RequestBuilder acceptLanguage(String... locales) {
        headers.put("Accept-Language", String.join(",", locales));
        return this;
    }

    @Override
    public RequestBuilder cookie(String name, String value) {
        return null;
    }

    @Override
    public RequestBuilder cookie(Cookie cookie) {
        return null;
    }

    @Override
    public RequestBuilder header(String name, Object value) {
        headers.put(name, value);
        return this;
    }

    @Override
    public RequestBuilder header(HeaderEnum header) {
        headers.put(header.getKey(), header.getValue());
        return this;
    }

    @Override
    public RequestBuilder headers(Map<String, Object> map) {
        headers.putAll(map);
        return this;
    }

    @Override
    public RequestBuilder headers(MultivaluedMap<String, Object> map) {
        headers.putAll(map);
        return this;
    }

    @Override
    public RequestBuilder queryParam(String key, String value) {
        return null;
    }

    @Override
    public RequestBuilder queryParamRaw(String key, String value) {
        return null;
    }

    @Override
    public RequestBuilder queryParams(Map<String, String> queryParams) {
        return null;
    }

    @Override
    public RequestBuilder queryParams(MultivaluedMap<String, String> queryParams) {
        return null;
    }

    @Override
    public RequestBuilder overrideHeader(String name, Object value) {
        return null;
    }

    @Override
    public RequestBuilder addBasicAuth(String username, String password) {
        return null;
    }

    @Override
    public RequestBuilder addBasicAuth(String username) {
        return null;
    }

    @Override
    public RequestBuilder addBearerToken(OAuth2Token token) {
        return null;
    }

    @Override
    public RequestBuilder removeAggregatorHeader() {
        return null;
    }

    @Override
    public void cutFilterTail() {}

    @Override
    public RequestBuilder addFilter(Filter filter) {
        return null;
    }

    @Override
    public RequestBuilder removeFilter(Filter filter) {
        return null;
    }

    @Override
    public boolean isFilterPresent(Filter filter) {
        return false;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }
}
