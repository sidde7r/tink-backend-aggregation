package se.tink.backend.system.cli.debug.provider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProviderStatusUpdaterTest {

    private static final ProviderStatuses TEST_STATUS = ProviderStatuses.DISABLED;

    private ProviderRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = mock(ProviderRepository.class);
    }

    @Test
    public void updatesProviderWhenFound() throws Exception {
        Provider provider = new Provider();
        when(repository.findByName(anyString())).thenReturn(provider);

        updateProviderStatus();

        ArgumentCaptor<Provider> argumentCaptor = ArgumentCaptor.forClass(Provider.class);
        verify(repository).saveAndFlush(argumentCaptor.capture());

        assertEquals(TEST_STATUS, argumentCaptor.getValue().getStatus());
    }

    @Test
    public void doesNothingIfProviderUnknown() throws Exception {
        when(repository.findByName(anyString())).thenReturn(null);

        updateProviderStatus();

        verify(repository, times(0)).saveAndFlush(any());
    }

    private void updateProviderStatus() {
        new ProviderStatusUpdater(repository).update("provider", TEST_STATUS);
    }
}
