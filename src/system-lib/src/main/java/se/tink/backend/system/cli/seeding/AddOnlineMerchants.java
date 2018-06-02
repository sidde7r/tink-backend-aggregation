package se.tink.backend.system.cli.seeding;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantSources;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

/**
 * Online merchants have been taken from the following sources
 * - Prisjakt.se => taken some top retailers in different categories
 * - Ehandelsbarometern => have checked this but did not find any list of top retailers
 * - Alexa.se => looked through top 250 sites for retailers
 */
public class AddOnlineMerchants extends ServiceContextCommand<ServiceConfiguration> {

    private MerchantRepository merchantRepository;
    private static final LogUtils log = new LogUtils(AddOnlineMerchants.class);

    public AddOnlineMerchants() {
        super("add-online-merchants", "Add online merchants");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        merchantRepository = serviceContext.getRepository(MerchantRepository.class);

        List<Merchant> existingMerchants = merchantRepository.findAll();
        Set<String> onlineMerchants = getOnlineMerchants();

        for (final String name : onlineMerchants) {

            boolean alreadyAdded = Iterables.any(existingMerchants,
                    merchant -> merchant.getName().equals(name) && merchant.getOnline());

            if (!alreadyAdded) {
                addMerchant(name);
            }

        }

    }

    private void addMerchant(String name) {

        Merchant newMerchant = new Merchant();

        String website = String.format("http://www.%s", name.trim().toLowerCase());

        newMerchant.setName(name);
        newMerchant.setWebsite(website);
        newMerchant.setFormattedAddress(website);
        newMerchant.setOnline(true);
        newMerchant.setSource(MerchantSources.MANUALLY); // Manually because source file is excluded in search

        log.info(String.format("Persisted [%s] with website [%s] in database", name, newMerchant.getWebsite()));

        merchantRepository.saveAndIndex(newMerchant);
    }

    private static HashSet<String> getOnlineMerchants() {

        HashSet<String> onlineMerchants = Sets.newHashSet();

        onlineMerchants.add("Adlibris.se");
        onlineMerchants.add("Amazon.com");
        onlineMerchants.add("Apollo.se");
        onlineMerchants.add("Apple.com");
        onlineMerchants.add("Atg.se");
        onlineMerchants.add("Betsson.se");
        onlineMerchants.add("Biltema.se");
        onlineMerchants.add("Blocket.se");
        onlineMerchants.add("Bokus.se");
        onlineMerchants.add("Boozt.com");
        onlineMerchants.add("Brandos.se");
        onlineMerchants.add("Bubbleroom.se");
        onlineMerchants.add("Bygghemma.se");
        onlineMerchants.add("Cdon.se");
        onlineMerchants.add("Cellbes.se");
        onlineMerchants.add("Clasohlson.se");
        onlineMerchants.add("Conrad.se");
        onlineMerchants.add("Coolstuff.se");
        onlineMerchants.add("Däckonline.se");
        onlineMerchants.add("Dell.se");
        onlineMerchants.add("Discshop.se");
        onlineMerchants.add("Dustin.se");
        onlineMerchants.add("Ebay.com");
        onlineMerchants.add("Elgiganten.se");
        onlineMerchants.add("Ellos.se");
        onlineMerchants.add("Elon.se");
        onlineMerchants.add("Expedia.se");
        onlineMerchants.add("Footway.se");
        onlineMerchants.add("Fritidsresor.se");
        onlineMerchants.add("Fyndiq.se");
        onlineMerchants.add("Ginza.se");
        onlineMerchants.add("Halens.se");
        onlineMerchants.add("Harrods.com");
        onlineMerchants.add("Hotels.com");
        onlineMerchants.add("HP.se");
        onlineMerchants.add("Ikea.se");
        onlineMerchants.add("InkClub.se");
        onlineMerchants.add("Intersport.se");
        onlineMerchants.add("Jula.se");
        onlineMerchants.add("Kicks.se");
        onlineMerchants.add("Komplett.se");
        onlineMerchants.add("Lekmer.se");
        onlineMerchants.add("LensWay.se");
        onlineMerchants.add("Liveit.se");
        onlineMerchants.add("Lyckasmedmat.se");
        onlineMerchants.add("Macys.com");
        onlineMerchants.add("Mediamarkt.se");
        onlineMerchants.add("Misco.se");
        onlineMerchants.add("Momondo.se");
        onlineMerchants.add("Nelly.se");
        onlineMerchants.add("NetFlix.se");
        onlineMerchants.add("NetOnNet.se");
        onlineMerchants.add("Norwegian.com");
        onlineMerchants.add("Parfym.se");
        onlineMerchants.add("Pixmania.se");
        onlineMerchants.add("Resia.se");
        onlineMerchants.add("Ridestore.se");
        onlineMerchants.add("Sas.se");
        onlineMerchants.add("Scandinavianphoto.se");
        onlineMerchants.add("Sf.se");
        onlineMerchants.add("Sfanytime.se");
        onlineMerchants.add("Siba.se");
        onlineMerchants.add("Smartguy.se");
        onlineMerchants.add("Sportamore.se");
        onlineMerchants.add("Stayhard.se");
        onlineMerchants.add("Stylepit.se");
        onlineMerchants.add("Svenskaspel.se");
        onlineMerchants.add("TailorStore.se");
        onlineMerchants.add("Teknikmagasinet.se");
        onlineMerchants.add("Ticket.se");
        onlineMerchants.add("Ticketmaster.com");
        onlineMerchants.add("Ticnet.se");
        onlineMerchants.add("Tradera.se");
        onlineMerchants.add("Travellink.se");
        onlineMerchants.add("Tretti.se");
        onlineMerchants.add("Uppercut.se");
        onlineMerchants.add("Viaplay.se");
        onlineMerchants.add("Ving.se");
        onlineMerchants.add("Webhallen.se");
        onlineMerchants.add("Zalando.se");

        return onlineMerchants;
    }
}



