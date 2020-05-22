package se.tink.backend.aggregation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;

public class SupplementalInformationControllerTest {

    private static CacheClient cacheClient;
    private static SupplementalInformationController sut;
    private String key = "tpcb_c143c468-0995-4032-8fc8-0b47f6c4feed";
    private Object value = "state:c143c468-0995-4032-8fc8-0b47f6c4feed";

    @Before
    public void setup() {
        cacheClient = mock(CacheClient.class);
        sut = new SupplementalInformationController(cacheClient, mock(CuratorFramework.class));
    }

    @Test
    public void testGetSupplementalInformationNormal() {
        when(cacheClient.get(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, key))
                .thenReturn(value);
        assertThat((String) value).isEqualTo(sut.getSupplementalInformation(key));
        verify(cacheClient, times(1))
                .delete(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, key);
    }

    @Test
    public void testGetSupplementalInformationNull() {
        when(cacheClient.get(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, key))
                .thenReturn(null);
        assertThat(sut.getSupplementalInformation(key)).isNull();
        verify(cacheClient, times(1))
                .delete(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, key);
    }

    @Test(expected = RuntimeException.class)
    public void testGetSupplementalInformationThrow() {
        when(cacheClient.get(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, key))
                .thenThrow(new RuntimeException());
        sut.getSupplementalInformation(key);
    }

    @Test
    public void testGetSupplementalInformationException() {
        when(cacheClient.get(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, key))
                .thenThrow(new RuntimeException());
        try {
            sut.getSupplementalInformation(key);
        } catch (Exception e) {
            verify(cacheClient, times(1))
                    .delete(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, key);
        }
    }
}
