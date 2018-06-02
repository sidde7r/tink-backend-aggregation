package se.tink.backend.client;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.core.oauth2.OAuth2Client;

public class CorsFilterAdder {

    private String domainString;
    private boolean isDevelopment;

    private static final Predicate<OAuth2Client> NOT_NULL_URLS = client -> !Strings.isNullOrEmpty(client.getUrl());

    private static final Function<OAuth2Client, String> CLIENT_TO_URL = new Function<OAuth2Client, String>() {
        @Nullable
        @Override
        public String apply(OAuth2Client client) {
            return client.getUrl();
        }
    };

    public CorsFilterAdder(ServiceContext context) {

        this.isDevelopment = context.getConfiguration().isDevelopmentMode();

        OAuth2ClientRepository oauthClients = context.getRepository(OAuth2ClientRepository.class);

        List<OAuth2Client> clients = oauthClients.findAll();

        // Add Tink apps to valid domains. This was previously done from proxy config.
        List<String> domains = Lists.newArrayList(
                "https://www.tinkapp.com",
                "https://oauth.tink.se",
                "https://oauth-flow.appspot.com",
                "https://account.staging.oxford.tink.se",
                "https://account.tink.se"
        );

        if (isDevelopment) {
            domains.add("*");
        }

        FluentIterable.from(clients)
                .filter(NOT_NULL_URLS)
                .transform(CLIENT_TO_URL)
                .copyInto(domains);

        domainString = Joiner.on(',').join(domains);
    }

    public void add(Environment environment) {
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", domainString);

        cors.setInitParameter("allowedHeaders",
                "X-Requested-With,Content-Type,Accept,Origin,Authorization,X-Tink-Device-ID,X-Tink-Client-Key,X-Tink-OAuth-Client-ID,X-Tink-Multi-Factor-Url,X-Tink-Token");
        cors.setInitParameter("exposedHeaders", "X-Tink-Multi-Factor-Url");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping (which of our endpoints this affects)
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
