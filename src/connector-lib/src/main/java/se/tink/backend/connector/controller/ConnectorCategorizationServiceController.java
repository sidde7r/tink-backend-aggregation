package se.tink.backend.connector.controller;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import se.tink.backend.categorization.api.CategorizationService;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.client.CategorizationServiceFactory;
import se.tink.backend.categorization.rpc.CategorizationLabel;
import se.tink.backend.categorization.rpc.CategorizationResult;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;

public class ConnectorCategorizationServiceController {
    private CategoryConfiguration categoryConfiguration;
    private CategorizationServiceFactory categorizationServiceFactory;

    @Inject
    public ConnectorCategorizationServiceController(CategoryConfiguration categoryConfiguration,
            CategorizationServiceFactory categorizationServiceFactory) {
        this.categoryConfiguration = categoryConfiguration;
        this.categorizationServiceFactory = categorizationServiceFactory;
    }

    public String category(String marketCode, String transactionDescription) throws RequestException {
        if(Strings.isNullOrEmpty(marketCode)) {
            throw RequestError.MARKET_NOT_FOUND.exception();
        }

        if(Strings.isNullOrEmpty(transactionDescription)) {
            throw RequestError.INVALID_DESCRIPTION.exception();
        }


        String categoryCode = categoryConfiguration.getExpenseUnknownCode();

        CategorizationService categorizationService = categorizationServiceFactory.getCategorizationService();
        CategorizationResult categorizationResult = categorizationService
                .category(marketCode, transactionDescription);
        if (!categorizationResult.getLabels().isEmpty()) {
            CategorizationLabel categorizationLabel = categorizationResult.getLabels().get(0);
            if (categorizationLabel.getPercentage() > 0.5d) {
                categoryCode = categorizationLabel.getLabel();
            }
        }

        return categoryCode;
    }
}
