package se.tink.libraries.http.client;

public interface ServiceClassBuilder {
    /**
     * Build a serviceClass. If there are multiple candidates, a random one is chosen.
     *
     * @param serviceClass
     * @return
     */
    <T> T build(Class<T> serviceClass);

    /**
     * Build a serviceClass. If there are mutliple candidates, <code>hash</code> will be used to try
     * to use the same candidate every time.
     *
     * @param serviceClass
     * @param hash
     * @return
     */
    <T> T build(Class<T> serviceClass, Object hashSource);
}
