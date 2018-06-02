package se.tink.backend.product.execution.unit.agents.seb.mortgage;

public interface HttpClient {
    <T> T get(ApiRequest request, Class<T> responseModel);

    <T> T post(ApiRequest request, Class<T> responseModel);
}
