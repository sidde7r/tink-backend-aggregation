package se.tink.backend.common.application;

import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.field.ApplicationFieldEnricher;
import se.tink.backend.common.application.field.ApplicationFieldFactory;
import se.tink.backend.common.application.field.ApplicationFieldTemplate;
import se.tink.backend.common.application.field.ApplicationFieldValidator;
import se.tink.backend.common.application.form.ApplicationFormEnricher;
import se.tink.backend.common.application.form.ApplicationFormValidator;
import se.tink.backend.common.application.mortgage.SwitchMortgageProviderApplicationValidator;
import se.tink.backend.common.application.mortgage.SwitchMortgageProviderEngine;
import se.tink.backend.common.application.mortgage.SwitchMortgageProviderFormValidator;
import se.tink.backend.common.application.mortgage.SwitchMortgageProviderValueApplier;
import se.tink.backend.common.application.savings.OpenSavingsAccountApplicationValidator;
import se.tink.backend.common.application.savings.OpenSavingsAccountEngine;
import se.tink.backend.common.application.savings.OpenSavingsAccountFormValidator;
import se.tink.backend.common.application.savings.OpenSavingsAccountValueApplier;
import se.tink.backend.common.application.valuation.ResidenceValuationApplicationValidator;
import se.tink.backend.common.application.valuation.ResidenceValuationEngine;
import se.tink.backend.common.application.valuation.ResidenceValuationFormValidator;
import se.tink.backend.common.application.valuation.ResidenceValuationValueApplier;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.core.Application;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;

public class ApplicationProcessorFactory {

    private final ServiceContext serviceContext;
    private final ProviderImageProvider providerImageProvider;
    private final ApplicationEnricher enricher;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public ApplicationProcessorFactory(ServiceContext serviceContext, ProviderImageProvider providerImageProvider) {

        this.serviceContext = serviceContext;
        this.providerImageProvider = providerImageProvider;

        ApplicationFieldEnricher fieldEnricher = new ApplicationFieldEnricher();
        ApplicationFormEnricher formEnricher = new ApplicationFormEnricher(fieldEnricher);

        this.deepLinkBuilderFactory = new DeepLinkBuilderFactory(
                serviceContext.getConfiguration().getNotifications().getDeepLinkPrefix());
        enricher = new ApplicationEnricher(formEnricher);
    }

    public ApplicationProcessor create(Application application, User user) {
        return create(application, user, null);
    }

    public ApplicationProcessor create(Application application, User user, TinkUserAgent userAgent) {
        ApplicationTemplate template = ApplicationTemplate.create(application);

        return new ApplicationProcessor(
                createValidator(template, user, userAgent),
                createEngine(template, user, userAgent),
                createValueApplier(template, userAgent),
                enricher,
                user);
    }

    private ApplicationEngine createEngine(ApplicationTemplate template, User user, TinkUserAgent userAgent) {
        
        ApplicationFieldTemplate fieldTemplate = new ApplicationFieldTemplate();
        ApplicationFieldFactory fieldFactory = new ApplicationFieldFactory(fieldTemplate);
        
        switch(template.getType()) {
        case SWITCH_MORTGAGE_PROVIDER:
            return new SwitchMortgageProviderEngine(serviceContext, fieldFactory, template, user,
                    deepLinkBuilderFactory, userAgent);
        case OPEN_SAVINGS_ACCOUNT:
            return new OpenSavingsAccountEngine(serviceContext, fieldFactory, template, user, deepLinkBuilderFactory);
        case RESIDENCE_VALUATION:
            return new ResidenceValuationEngine(serviceContext, fieldFactory, template, user);
        default:
            throw new IllegalArgumentException("Type not implemented");
        }
    }

    private ApplicationFormValidator createFormValidator(ApplicationTemplate template, User user) {
        
        ApplicationFieldTemplate fieldTemplate = new ApplicationFieldTemplate();
        ApplicationFieldValidator fieldValidator = new ApplicationFieldValidator(fieldTemplate, user);
        
        switch (template.getType()) {
        case SWITCH_MORTGAGE_PROVIDER:
            return new SwitchMortgageProviderFormValidator(template, user, fieldValidator,
                    serviceContext.getRepository(CurrencyRepository.class));
        case OPEN_SAVINGS_ACCOUNT:
            return new OpenSavingsAccountFormValidator(template, user, fieldValidator);
        case RESIDENCE_VALUATION:
            return new ResidenceValuationFormValidator(template, user, fieldValidator);
        default:
            throw new IllegalArgumentException("Type not implemented");
        }
    }

    private ApplicationValidator createValidator(ApplicationTemplate template, User user, TinkUserAgent userAgent) {
        switch (template.getType()) {
        case SWITCH_MORTGAGE_PROVIDER:
            return new SwitchMortgageProviderApplicationValidator(template, user, createFormValidator(template, user),
                    userAgent);
        case OPEN_SAVINGS_ACCOUNT:
            return new OpenSavingsAccountApplicationValidator(template, user, createFormValidator(template, user));
        case RESIDENCE_VALUATION:
            return new ResidenceValuationApplicationValidator(template, user, createFormValidator(template, user));
        default:
            throw new IllegalArgumentException("Type not implemented");
        }
    }

    private ApplicationValueApplier createValueApplier(ApplicationTemplate template, TinkUserAgent userAgent) {
        switch(template.getType()) {
        case SWITCH_MORTGAGE_PROVIDER:
            return new SwitchMortgageProviderValueApplier(serviceContext, providerImageProvider,
                    serviceContext.getEventTracker(), userAgent, deepLinkBuilderFactory,
                    serviceContext.getAggregationControllerCommonClient(), serviceContext.isProvidersOnAggregation());
        case OPEN_SAVINGS_ACCOUNT:
            return new OpenSavingsAccountValueApplier(serviceContext, providerImageProvider, deepLinkBuilderFactory,
                    serviceContext.getAggregationControllerCommonClient(), serviceContext.isProvidersOnAggregation());
        case RESIDENCE_VALUATION:
            return new ResidenceValuationValueApplier(serviceContext, providerImageProvider);
        default:
            throw new IllegalArgumentException("Type not implemented");
        }
    }
}
