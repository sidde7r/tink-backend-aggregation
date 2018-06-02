package se.tink.backend.main.resources;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import org.apache.http.ParseException;
import org.json.JSONException;
import se.tink.backend.api.MerchantService;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.merchants.MerchantSearcher;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.common.providers.MarketProvider;
import se.tink.backend.common.repository.cassandra.MerchantWizardSkippedTransactionRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Market;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantSources;
import se.tink.backend.core.Place;
import se.tink.backend.core.StringStringPair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.merchants.MerchantWizardSkippedTransaction;
import se.tink.backend.main.controllers.CategoryController;
import se.tink.backend.main.controllers.MarketServiceController;
import se.tink.backend.rpc.MerchantQuery;
import se.tink.backend.rpc.MerchantQueryResponse;
import se.tink.backend.rpc.MerchantSkipRequest;
import se.tink.backend.rpc.MerchantizeTransactionsRequest;
import se.tink.backend.rpc.SuggestMerchantizeRequest;
import se.tink.backend.rpc.SuggestMerchantizeResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.MerchantUtils;
import se.tink.backend.utils.StringUtils;

@Path("/api/v1/merchants")
public class MerchantServiceResource implements MerchantService {

    @Context
    private HttpHeaders headers;

    private final ServiceContext serviceContext;
    private final CacheClient cacheClient;
    private final CategoryController categoryController;
    private final MarketServiceController marketServiceController;

    private MerchantSearcher merchantSearcher;
    private static final LogUtils log = new LogUtils(MerchantServiceResource.class);

    private final TransactionDao transactionDao;
    private final MerchantRepository merchantRepository;
    private final MerchantWizardSkippedTransactionRepository skippedTransactionRepository;;
    private final UserStateRepository userStateRepository;

    public MerchantServiceResource(ServiceContext serviceContext, ElasticSearchClient elasticSearchClient) {
        this.serviceContext = serviceContext;
        this.cacheClient = serviceContext.getCacheClient();
        this.categoryController = new CategoryController(
                serviceContext.getRepository(CategoryRepository.class)
        );
        this.marketServiceController = new MarketServiceController(new MarketProvider(
                serviceContext.getRepository(MarketRepository.class),
                serviceContext.getRepository(CurrencyRepository.class),
                serviceContext.getConfiguration().getAuthentication()));

        merchantSearcher = new MerchantSearcher(serviceContext, elasticSearchClient);

        this.transactionDao = serviceContext.getDao(TransactionDao.class);
        this.merchantRepository = serviceContext.getRepository(MerchantRepository.class);
        this.skippedTransactionRepository = serviceContext
                .getRepository(MerchantWizardSkippedTransactionRepository.class);
        this.userStateRepository = serviceContext.getRepository(UserStateRepository.class);
    }

    @Override
    public Merchant merchantize(User user, MerchantizeTransactionsRequest merchantizeRequests) {
        if (merchantizeRequests == null) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        if (merchantizeRequests.getMerchant() == null) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        if (merchantizeRequests.getMerchant().getName() == null) {
            log.error(user.getId(), "No name specified on merchant.");
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        Merchant createMerchant = merchantizeRequests.getMerchant();

        Merchant merchant;

        // We have a local merchant already
        if (createMerchant.getId() != null) {
            merchant = getLocalMerchant(createMerchant.getId());

        }
        // We have either a google merchant or a manually created merchant with address reference
        else if (createMerchant.getReference() != null) {

            // Should only be one entity that matches but keeping this logic since we have
            // some duplicates in production
            List<Merchant> localMerchants = merchantRepository
                    .findAllByReferenceAndSource(createMerchant.getReference(), MerchantSources.GOOGLE);

            if (localMerchants.size() > 0) {
                // We already have the merchant locally
                merchant = pickBestMatchedMerchant(localMerchants, createMerchant.getName());
            } else {
                // Don't have the merchant locally and need to create it
                merchant = createMerchantFromGooglePlaceId(user, createMerchant);
            }

        } else {

            // Local merchant without address
            merchant = createMerchantWithoutAddress(user, createMerchant);
        }

        setMerchantOnTransactions(user, merchant, merchantizeRequests.getTransactionIds());

        userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);

        return merchant;
    }

    private Merchant getLocalMerchant(String merchantId) {
        Merchant merchant = merchantRepository.findOne(merchantId);

        if (merchant == null) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        return merchant;
    }

    private Merchant createMerchantFromGooglePlaceId(User user, Merchant createMerchant) {
        try {
            Market market = marketServiceController.getMarket(user.getProfile().getMarket());

            Place place = merchantSearcher.getGooglePlacesSearcher().details(createMerchant.getReference(), market.getDefaultLocale());

            if (place == null) {
                HttpResponseHelper.error(Status.INTERNAL_SERVER_ERROR);
            }

            MerchantSources source = place.isEstablishment() ? MerchantSources.GOOGLE : MerchantSources.MANUALLY;

            Merchant merchant = MerchantUtils.mergePlaceWithMerchant(createMerchant, place, source);

            // Manually created merchants are only visible to the user that created it
            if (source == MerchantSources.MANUALLY) {
                merchant.setName(createMerchant.getName());
                merchant.setVisibleToUsers(Lists.newArrayList(user.getId()));
            }

            return saveAndIndexMerchant(user, merchant);

        } catch (ParseException | IOException | JSONException e) {
            log.error(user.getId(), "Could not look up merchant on create " + createMerchant.getReference(), e);
        }

        return null;
    }

    private Merchant pickBestMatchedMerchant(List<Merchant> merchants, String name) {
        Merchant bestMerchant = null;
        float bestMerchantScore = -1;

        for (Merchant m : merchants) {
            double similarity = StringUtils.getJaroWinklerDistance(m.getName(), name);

            // We have a better match
            if (similarity > bestMerchantScore) {
                bestMerchant = m;
            }

        }

        return bestMerchant;
    }

    private Merchant createMerchantWithoutAddress(User user, Merchant merchant) {

        merchant.setSource(MerchantSources.MANUALLY);
        merchant.setVisibleToUsers(Lists.newArrayList(user.getId()));

        return saveAndIndexMerchant(user, merchant);
    }

    private Merchant saveAndIndexMerchant(User user, Merchant merchant) {

        String visibility = merchant.getVisibleToUsersSerialized();

        log.info(user.getId(),
                String.format("Creating new merchant: [%s] Source: [%s] Visibility: [%s]", merchant.getName(),
                        merchant.getSource(), visibility == null ? "ALL" : visibility));

        return merchantRepository.saveAndIndex(merchant);
    }

    @Override
    public MerchantQueryResponse query(User user, MerchantQuery request) {
        MerchantQueryResponse response = new MerchantQueryResponse();
        List<Merchant> merchants = Lists.newArrayList();

        Market market = marketServiceController.getMarket(user.getProfile().getMarket());

        try {
            if (!Strings.isNullOrEmpty(request.getId())) {

                // Id query.

                log.info(user.getId(), "Querying merchant with id: " + request.getId());
                merchants.add(merchantRepository.findOne(request.getId()));

            } else if (!Strings.isNullOrEmpty(request.getReference())) {

                // Place ID query.

                log.info(user.getId(), "Querying merchants with reference: " + request.getReference());
                List<Merchant> localMerchants = merchantRepository.findAllByReference(request.getReference());

                if (localMerchants.size() > 0) {
                    merchants.addAll(localMerchants);
                } else {
                    Place place = merchantSearcher.getGooglePlacesSearcher().details(request.getReference(), market.getDefaultLocale());
                    merchants.add(MerchantUtils.createMerchant(place, MerchantSources.GOOGLE));
                }

            } else if (!Strings.isNullOrEmpty(request.getQueryString())) {
                // QueryString query.
                merchants.addAll(merchantSearcher.query(user, request, market.getDefaultLocale(), market.getCodeAsString()));
            }

        } catch (Exception e) {
            log.error(user.getId(), "Could not execute search", e);
        }

        response.setMerchants(Lists.newArrayList(
                Iterables.limit(merchants, request.getLimit() != 0 ? request.getLimit() : Integer.MAX_VALUE)));

        log.info(user.getId(), "Returning " + response.getMerchants().size() + " merchants");
        return response;
    }

    @Override
    public Merchant get(User user, String id) {
        if (Strings.isNullOrEmpty(id)) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        Merchant merchant = merchantRepository.findOne(id);

        if (merchant == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        return merchant;
    }

    @Override
    public MerchantQueryResponse address(User user, MerchantQuery request) {
        if (request == null || Strings.isNullOrEmpty(request.getQueryString())) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        Market market = marketServiceController.getMarket(user.getProfile().getMarket());

        String queryString = request.getQueryString().trim();

        MerchantQueryResponse response = new MerchantQueryResponse();
        try {
            List<StringStringPair> addresses = merchantSearcher.getGooglePlacesSearcher()
                    .autocompleteAddress(queryString, request.getLimit(), market.getDefaultLocale(), market.getCodeAsString());

            List<Merchant> merchants = Lists.newArrayList();

            for (StringStringPair pair : addresses) {
                Merchant merchant = new Merchant();
                merchant.setFormattedAddress(pair.getValue());
                merchant.setReference(pair.getKey());
                merchants.add(merchant);
            }
            response.setMerchants(merchants);
        } catch (Exception e) {
            log.error("Could not auto complete address " + queryString, e);
        }
        log.info(user.getId(), "Returning " + response.getMerchants().size() + " results from query: " + queryString);
        return response;
    }

    @Override
    public SuggestMerchantizeResponse suggest(User user, SuggestMerchantizeRequest request) {
        Market market = marketServiceController.getMarket(user.getProfile().getMarket());

        // Data is fetched on each card on Ios but not on Android.
        // Use this check until we have the same logic on both devices

        boolean includeMerchantsInResult = RequestHeaderUtils.isAndroidRequest(headers);

        return merchantSearcher.suggest(user, request,
                Lists.newArrayList(
                        categoryController.localeCategoriesToIds(user.getProfile().getLocale()).values()),
                includeMerchantsInResult, market.getDefaultLocale(), market.getCodeAsString());
    }

    /**
     * Updated transactions with merchantId both in database and index.
     *
     * @param user
     * @param merchant
     * @param transactionIds
     */
    private void setMerchantOnTransactions(User user, Merchant merchant, List<String> transactionIds) {

        log.info(user.getId(),
                "Merchantizing " + transactionIds.size() + " transaction(s) with merchant: " + merchant.getName());

        List<Transaction> requestTransactions = transactionDao.findByUserIdAndId(user, transactionIds);
        final List<Transaction> transactions = Lists.newArrayList();

        if (requestTransactions.size() != transactionIds.size()) {
            for (String transactionId : transactionIds) {
                log.error("Could not merchantize transactions, no transaction for Id: " + transactionId);
            }
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        for (Transaction transaction : requestTransactions) {
            transaction.changeMerchant(merchant);
            transaction.setUserModifiedLocation(true);
            transactions.add(transaction);
        }

        // Group by description
        ImmutableListMultimap<String, Transaction> groupedByDescription = Multimaps
                .index(requestTransactions, Transaction::getDescription);

        for (String description : groupedByDescription.keys()) {
            // Batch update the merchant change.
            // Note that userModifiedLocation also is updated
            transactionDao.updateMerchantIdAndDescription(user, groupedByDescription.get(description).stream()
                            .collect(Collectors.toMap(Transaction::getId, getPeriod)),
                    merchant.getId(), description);
        }

        // Flush to index (synchronous if single, asynchronous if multiple).
        if (transactions.size() == 1) {
            transactionDao.index(transactions, true);
        } else {
            serviceContext.execute(() -> transactionDao.index(transactions, false));
        }
    }

    @Override
    public void skip(User user, MerchantSkipRequest request) {

        if (request == null || request.getTransactionIds() == null) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        if (request.getTransactionIds().size() == 0) {
            return;
        }

        try {
            final UUID userId = UUIDUtils.fromTinkUUID(user.getId());

            Iterable<MerchantWizardSkippedTransaction> entities = Iterables
                    .transform(request.getTransactionIds(), transactionId -> {
                        MerchantWizardSkippedTransaction entity = new MerchantWizardSkippedTransaction();
                        entity.setInserted(new Date());
                        entity.setUserId(userId);
                        entity.setTransactionId(UUIDUtils.fromTinkUUID(transactionId));
                        return entity;
                    });

            skippedTransactionRepository.saveInBatches(entities);

        } catch (Exception e) {
            log.error(user.getId(), "Could not skip transactions", e);
        }

    }

    private Function<Transaction, Integer> getPeriod = (Transaction t) -> {
        LocalDate localDate = t.getOriginalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Integer period = DateUtils.getYearMonth(localDate);
        return period;
    };

}
