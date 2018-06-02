package se.tink.backend.system.cli.seeding;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.merchants.GooglePlacesSearcher;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantSources;
import se.tink.backend.core.Place;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.MerchantUtils;
import se.tink.backend.utils.StringUtils;

public class SeedMerchantsWithPropertiesCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final String DEFAULT_LOCALE = "sv_SE";
    private static final String DEFAULT_COUNTRY = "SE";

    private static final LogUtils log = new LogUtils(SeedMerchantsWithPropertiesCommand.class);
    
    public SeedMerchantsWithPropertiesCommand() {
        super("seed-merchant-with-properties", "Sets google propertied on merchants");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        MerchantRepository merchantRepositiry = serviceContext.getRepository(MerchantRepository.class);
        
        List<Merchant> merchants  = merchantRepositiry.findAll();
        
        GooglePlacesSearcher googlePlaces = new GooglePlacesSearcher();
        
        int count = 0;
        int updatedCount = 0;

        log.info("Doing " + merchants.size() + " merchants...");
        for (Merchant m : merchants) {
            log.info("");
            log.info(m.getName());
            Thread.sleep(100);
            try {
                if (Strings.isNullOrEmpty(m.getReference())) {
                    continue;
                }
                
                count++;
                
                // Try if reference is right. 
                
                Place place = googlePlaces.detailsoOnReference(m.getReference(), DEFAULT_LOCALE);

                if (place != null) {
                    log.info("\t" + place.getName());
                }
                 
                // Try a auto complete with name. 
                
                if (place == null || StringUtils.getJaroWinklerDistance(place.getName(), m.getName()) < 0.85) {

                    place = null;
                    
                    List<Place> places = googlePlaces.detailedAutocomplete(m.getName(), 5, DEFAULT_LOCALE, DEFAULT_COUNTRY);
                    
                    for (Place gm : places) {
                        log.info("\t" + gm.getName());
                        if (StringUtils.getJaroWinklerDistance(gm.getName(), m.getName()) > 0.85) {
                            place = gm;
                            break;
                        }
                    }
                    
                    // Try a text search with name 
                    
                    if (place == null) {
                        places = googlePlaces.textSearch(m.getName(), DEFAULT_LOCALE, DEFAULT_COUNTRY);
                        
                        for (Place gm : places) {
                            log.info("\t" + gm.getName());
                            if (StringUtils.getJaroWinklerDistance(gm.getName(), m.getName()) > 0.85) {
                                place = gm;
                                break;
                            }
                        }
                    }
                }
                
                // If not found, then we cannot find it on google. 
                
                if (place == null) {
                    m.setSource(MerchantSources.MANUALLY);
                    m.setReference(null);
                } else {
                    log.info("MATCH: " + place.getName());
                    m = MerchantUtils.mergePlaceWithMerchant(m, place, MerchantSources.GOOGLE);
                    updatedCount++;
                }
                                
                merchantRepositiry.save(m);
                
                if (count % 10000 == 0) {
                    log.info(String.format("Have done %d merchants", count));
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("Total: " + count);
        log.info("Updated merchants: " + updatedCount);
    }

}
