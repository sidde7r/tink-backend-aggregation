package se.tink.backend.system.cli.reporting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.system.cli.CliPrintUtils;
import se.tink.backend.system.cli.ServiceContextCommand;

public class OAuthClientInformationCommand extends ServiceContextCommand<ServiceConfiguration> {

    public OAuthClientInformationCommand() {
        super("oauth-client-info", "");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        OAuth2ClientRepository oAuth2ClientRepository = serviceContext.getRepository(OAuth2ClientRepository.class);

        List<OAuth2Client> clients = oAuth2ClientRepository.findAll();

        CliPrintUtils.printTable(createOutput(clients));
    }

    private List<Map<String, String>> createOutput(List<OAuth2Client> clients) {
        return Lists.transform(clients, input -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("id", input.getId());
            data.put("name", input.getName());
            data.put("scope", input.getScope());

            data.put("AUTO_AUTHORIZE", input.getPayloadValue(
                    OAuth2Client.PayloadKey.AUTO_AUTHORIZE).orElse(null));
            data.put("DOESNT_PRODUCE_TINK_USERS", input.getPayloadValue(
                    OAuth2Client.PayloadKey.DOESNT_PRODUCE_TINK_USERS).orElse(null));
            data.put("ALLOW_DEMO_CREDENTIALS", input.getPayloadValue(
                    OAuth2Client.PayloadKey.ALLOW_DEMO_CREDENTIALS).orElse(null));
            data.put("REFRESHABLE_ITEMS", input.getPayloadValue(
                    OAuth2Client.PayloadKey.REFRESHABLE_ITEMS).orElse(null));

            return data;
        });
    }
}
