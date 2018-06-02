package se.tink.backend.connector.controller;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.categorization.api.CategorizationService;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.categorization.client.CategorizationServiceFactory;
import se.tink.backend.categorization.rpc.CategorizationLabel;
import se.tink.backend.categorization.rpc.CategorizationResult;
import se.tink.backend.connector.exception.RequestException;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectorCategorizationServiceControllerTest {
    private static final String MARKET = "SE";

    private ConnectorCategorizationServiceController controller;
    private CategoryConfiguration categoryConfiguration;
    private CategorizationResult categorizationResult;

    @Before
    public void setup() {
        CategorizationServiceFactory categorizationServiceFactory = mock(CategorizationServiceFactory.class);
        CategorizationService categorizationService = mock(CategorizationService.class);
        categorizationResult = mock(CategorizationResult.class);
        when(categorizationServiceFactory.getCategorizationService()).thenReturn(categorizationService);
        when(categorizationService.category(Mockito.anyString(), Mockito.anyString())).thenReturn(categorizationResult);

        categoryConfiguration = new SECategories();
        controller = new ConnectorCategorizationServiceController(categoryConfiguration, categorizationServiceFactory);
    }

    @Test
    public void categoryOnCorrectResponse() throws RequestException {
        CategorizationLabel categorizationLabel = mock(CategorizationLabel.class);
        when(categorizationLabel.getLabel()).thenReturn(categoryConfiguration.getBarsCode());
        when(categorizationLabel.getPercentage()).thenReturn(1.0);

        when(categorizationResult.getLabels()).thenReturn(Collections.singletonList(categorizationLabel));

        String category = controller.category(MARKET, "Should be bars");
        assertEquals(categoryConfiguration.getBarsCode(), category);
    }

    @Test
    public void uncategorizedOnLowProbability() throws RequestException {
        CategorizationLabel categorizationLabel = mock(CategorizationLabel.class);
        when(categorizationLabel.getLabel()).thenReturn(categoryConfiguration.getBarsCode());
        when(categorizationLabel.getPercentage()).thenReturn(0.1);

        when(categorizationResult.getLabels()).thenReturn(Collections.singletonList(categorizationLabel));

        String category = controller.category(MARKET, "Should be uncategorized");
        assertEquals(categoryConfiguration.getExpenseUnknownCode(), category);
    }
}

