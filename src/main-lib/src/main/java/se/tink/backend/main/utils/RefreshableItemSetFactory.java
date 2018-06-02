package se.tink.backend.main.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2Utils;

public class RefreshableItemSetFactory {

    private static final Splitter SPLITTER = Splitter.on(',').trimResults();

    @Inject
    public RefreshableItemSetFactory() {
    }

    public Optional<Set<RefreshableItem>> createSetForOauth2Client(Optional<OAuth2Client> oauth2Client) {

        if (!oauth2Client.isPresent()) {
            return Optional.empty();
        }

        Set<RefreshableItem> itemsToReturn = Sets.newHashSet();
        Optional<String> items = OAuth2Utils.getPayloadValue(oauth2Client, OAuth2Client.PayloadKey.REFRESHABLE_ITEMS);
        if (items.isPresent()) {
            Iterable<String> itemNames = SPLITTER.split(items.get());
            for (String name : itemNames) {
                itemsToReturn.add(RefreshableItem.valueOf(name));
            }
        }

        return Optional.of(itemsToReturn);
    }
}
