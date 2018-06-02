package se.tink.backend.common.utils;

import com.google.inject.Inject;
import javax.inject.Named;

public class DeepLinkBuilderFactory {

    private final String prefix;

    private static final String FOLLOW = "follow";
    private static final String FOLLOW_WITH_ID = "follow/%s";
    private static final String LEFT_TO_SPEND = "left-to-spend";
    private static final String TRACKING = "tracking";
    private static final String TRANSACTIONS = "transactions/%s";
    private static final String TRANSACTION_SKIP = "transactions/%s/link/skip";
    private static final String ACCOUNT_WITH_ID = "accounts/%s";
    private static final String ACCOUNT = "accounts";
    private static final String FRAUD = "fraud";
    private static final String SUGGEST_MERCHANT = "suggest/merchants/%s";
    private static final String SUGGEST_CATEGORY = "suggest/categories";
    private static final String SEARCH = "search";
    private static final String SHAREABLE_HTML = "shareable-html/%s";
    private static final String PROVIDER = "providers/%s";
    private static final String ADD_PROVIDER = "providers/%s/add";
    private static final String CATEGORY = "categories/%s";
    private static final String TRANSFER_POST = "transfer/%s";
    private static final String CREATE_APPLICATION = "applications/create/%s";
    private static final String APPLICATION = "applications/%s";
    private static final String EDIT_CREDENTIALS = "credentials/%s/edit";
    private static final String OPEN = "open";
    private static final String CAMPAIGN = "campaigns";
    private static final String SHOW_FULL_APPLICATION = "local/show-full-application";
    private static final String IS_PEP = "local/is-pep";
    private static final String TAXABLE_IN_OTHER_COUNTRY = "local/taxable-in-other-country";
    private static final String PARTNERS = "local/partners";
    private static final String RATE_THIS_APP = "rate-this-app";

    @Inject
    public DeepLinkBuilderFactory(@Named("deepLinkPrefix") String prefix) {
        this.prefix = prefix;
    }

    public class Builder {

        private StringBuilder deepLink;
        private boolean haveAppendedParameters = false;

        public Builder(String link) {
            this.deepLink = new StringBuilder(prefix + link);
        }

        public Builder withSource(String source) {
            if (source != null) {
                deepLink.append(queryParameter("source", source));
            }
            return this;
        }

        public Builder withPeriod(String period) {
            if (period != null) {
                deepLink.append(queryParameter("period", period));
            }
            return this;
        }

        public Builder withCampaign(String campaign) {
            if (campaign != null) {
                deepLink.append(queryParameter("campaign", campaign));
            }
            return this;
        }

        public Builder withMedium(String medium) {
            if (medium != null) {
                deepLink.append(queryParameter("medium", medium));
            }
            return this;
        }

        public Builder withQuery(String query) {
            if (query != null) {
                deepLink.append(queryParameter("query", query));
            }
            return this;
        }

        public Builder withType(String type) {
            if (type != null) {
                deepLink.append(queryParameter("type", type));
            }
            return this;
        }

        public Builder withStatus(String status) {
            if (status != null) {
                deepLink.append(queryParameter("status", status));
            }
            return this;
        }

        public String build() {
            return deepLink.toString();
        }

        private String queryParameter(String name, String value) {
            String separator = haveAppendedParameters ? "&" : "/?";
            haveAppendedParameters = true;
            return separator + name + "=" + value;
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public Builder eInvoices(String transferId) {
        return new Builder(String.format(TRANSFER_POST, transferId));
    }

    public Builder transaction(String id) {
        return new Builder(String.format(TRANSACTIONS, id));
    }

    public Builder transactionSkipLink(String id) {
        return new Builder(String.format(TRANSACTION_SKIP, id));
    }

    public Builder tracking() {
        return new Builder(TRACKING);
    }

    public Builder category(String categoryRef) {
        return new Builder(String.format(CATEGORY, categoryRef));
    }

    public Builder leftToSpend() {
        return new Builder(LEFT_TO_SPEND);
    }

    public Builder follow() {
        return new Builder(FOLLOW);
    }

    public Builder follow(String id) {
        return new Builder(String.format(FOLLOW_WITH_ID, id));
    }

    public Builder account(String id) {
        return new Builder(String.format(ACCOUNT_WITH_ID, id));
    }

    public Builder account() {
        return new Builder(ACCOUNT);
    }

    public Builder fraud() {
        return new Builder(FRAUD);
    }

    public Builder suggestMerchant(String id) {
        return new Builder(String.format(SUGGEST_MERCHANT, id));
    }

    public Builder search() {
        return new Builder(SEARCH);
    }

    public Builder shareableHtml(String id) {
        return new Builder(String.format(SHAREABLE_HTML, id));
    }

    public Builder addProvider(String id) {
        return new Builder(String.format(ADD_PROVIDER, id));
    }

    public Builder createApplication(String type) {
        return new Builder(String.format(CREATE_APPLICATION, type));
    }

    public Builder application(String id) {
        return new Builder(String.format(APPLICATION, id));
    }

    public Builder editCredentials(String id) {
        return new Builder(String.format(EDIT_CREDENTIALS, id));
    }

    public Builder suggestCategory() {
        return new Builder(SUGGEST_CATEGORY);
    }

    public Builder campaign() {
        return new Builder(CAMPAIGN);
    }

    public Builder showFullApplication() {
        return new Builder(SHOW_FULL_APPLICATION);
    }

    public Builder isPep() {
        return new Builder(IS_PEP);
    }

    public Builder taxableInOtherCountry() {
        return new Builder(TAXABLE_IN_OTHER_COUNTRY);
    }

    public Builder partners() {
        return new Builder(PARTNERS);
    }

    public Builder provider(String id) {
        return new Builder(String.format(PROVIDER, id));
    }

    public Builder open() {
        return new Builder(OPEN);
    }

    public Builder rateThisApp() {
        return new Builder(RATE_THIS_APP);
    }

    public Builder manualRefreshReminder(int daysSinceUpdated) {
        return new Builder(OPEN)
                .withSource("tink")
                .withMedium("notification")
                .withCampaign("manual-refresh-reminder-" + daysSinceUpdated);
    }

    public Builder paydayReminder() {
        return new Builder(OPEN)
                .withSource("tink")
                .withMedium("notification")
                .withCampaign("payday-reminder");
    }

    public Builder groupedEvents() {
        return new Builder(OPEN)
                .withSource("tink")
                .withMedium("notification")
                .withCampaign("grouped");
    }

    public Builder fraudWarning() {
        return new Builder(FRAUD)
                .withSource("tink")
                .withMedium("feed-activity")
                .withCampaign("fraud-warning");
    }

    public Builder fraudReminder() {
        return new Builder(FRAUD)
                .withSource("tink")
                .withMedium("notification")
                .withCampaign("fraud-reminder");
    }
}
