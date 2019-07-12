package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.depot;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Instrument;

public class MT535Instrument {

    private String finSeg;
    private String isin;
    private String marketPlace;
    private String country;
    private String ticker;
    private String name;
    private String priceSymbol = "EUR";
    private Date priceDate;
    private double marketPrice = 0.0;
    private double pieces = 0.0;
    private double totalValue = 0.0;
    private BigDecimal acquisitionPrice = BigDecimal.ZERO;

    private static final String MT535_FIN_SEGMENT_END = ":16S:FIN";
    private static final Pattern IDENTIFICATION =
            Pattern.compile("^:35B:ISIN\\s(.*)\\|\\/(.*)\\/(.*)\\|(.*)$");
    private static final Pattern MARKET_PRICE =
            Pattern.compile("^:90B::MRKT\\/\\/ACTU\\/([A-Z]{3})(.*)$");
    private static final Pattern MARKET_PLACE =
            Pattern.compile("^:94B::PRIC\\/\\/LMAR\\/([A-Z]{4})$");
    private static final Pattern PRICE_DATE = Pattern.compile("^:98A::PRIC\\/\\/(\\d*)$");
    private static final Pattern PIECES = Pattern.compile("^:93B::AGGR\\/\\/UNIT\\/(.*)$");
    private static final Pattern TOTAL_VALUE = Pattern.compile("^:19A::HOLD\\/\\/([A-Z]{3})(.*)$");
    private static final Pattern ACQUISITION_PRICE =
            Pattern.compile("^:70E::HOLD\\/\\/.*\\|(.*)\\+([A-Z]{3})$");

    private static final Logger log = LoggerFactory.getLogger(MT535Instrument.class);

    public MT535Instrument(String finSeg) {
        this.finSeg = finSeg;
    }

    public Instrument toTinkInstrument() {
        parse(collapseMultilinse(finSeg));

        Instrument instrument = new Instrument();
        instrument.setCurrency(priceSymbol);
        instrument.setIsin(isin);
        instrument.setMarketPlace(marketPlace);
        instrument.setUniqueIdentifier(isin + country + ticker);
        instrument.setAverageAcquisitionPrice(
                acquisitionPrice == null ? new BigDecimal(0) : acquisitionPrice);
        instrument.setQuantity(pieces);
        instrument.setPrice(marketPrice);
        instrument.setMarketValue(instrument.getQuantity() * instrument.getPrice());
        instrument.setProfit(instrument.getMarketValue() - instrument.getAverageAcquisitionPrice());
        instrument.setTicker(ticker);

        if (name != null) {
            instrument.setName(name);
            if (name.contains("Aktie")) {
                instrument.setType(Instrument.Type.STOCK);
                instrument.setRawType("Aktie");
            } else if (name.contains("Fond")) {
                instrument.setType(Instrument.Type.FUND);
                instrument.setRawType("Fond");
            } else if (name.contains("ETF")) {
                instrument.setType(Instrument.Type.OTHER);
                instrument.setRawType("ETF");
            } else if (name.contains("ETN")) {
                instrument.setType(Instrument.Type.OTHER);
                instrument.setRawType("ETN");
            } else if (name.contains("ETC")) {
                instrument.setType(Instrument.Type.OTHER);
                instrument.setRawType("ETC");
            }
        }

        return instrument;
    }

    private static List<String> collapseMultilinse(String str) {
        List<String> clauses = new ArrayList<>();
        String preLine = "";
        Scanner mt535Scanner = new Scanner(str);
        while (mt535Scanner.hasNextLine()) {
            String line = mt535Scanner.nextLine();
            if (line.startsWith(":")) {
                if (preLine != "") {
                    clauses.add(preLine);
                } else if (line.startsWith(MT535_FIN_SEGMENT_END)) {
                    // last line
                    clauses.add(preLine);
                    clauses.add(line);
                }
                preLine = line;
            } else {
                preLine += String.format("|%s", line);
            }
        }
        return clauses;
    }

    private void parse(List<String> clauses) {
        Matcher m;
        for (String clause : clauses) {
            // identification of instrument
            // e.g. ':35B:ISIN LU0635178014|/DE/ETF127|COMS.-MSCI EM.M.T.U.ETF I'
            m = IDENTIFICATION.matcher(clause);
            if (m.find()) {
                isin = m.group(1);
                country = m.group(2);
                ticker = m.group(3);
                name = m.group(4);
            }

            // current market price
            // e.g. ':90B::MRKT//ACTU/EUR52,7'
            m = MARKET_PRICE.matcher(clause);
            if (m.find()) {
                priceSymbol = m.group(1);
                marketPrice = Double.parseDouble(m.group(2).replace(",", "."));
            }

            // market place
            // e.g. ':94B::PRIC//LMAR/XFRA'
            m = MARKET_PLACE.matcher(clause);
            if (m.find()) {
                marketPlace = m.group(1);
            }

            // date of market price
            // e.g. ':98A::PRIC//20170428'
            m = PRICE_DATE.matcher(clause);
            if (m.find()) {
                try {
                    priceDate =
                            new SimpleDateFormat("yyyymmdd")
                                    .parse(Optional.ofNullable(m.group(1)).orElse(""));
                } catch (ParseException e) {
                    log.error("Unable to parse the price date from {} : {}", clause, e);
                }
            }

            // number of pieces
            // e.g. ':93B::AGGR//UNIT/100,'
            m = PIECES.matcher(clause);
            if (m.find()) {
                pieces = Double.parseDouble(m.group(1).replace(",", "."));
            }

            // total value of holding
            // e.g. ':19A::HOLD//EUR5270,'
            m = TOTAL_VALUE.matcher(clause);
            if (m.find()) {
                totalValue = Double.parseDouble(m.group(2).replace(",", "."));
            }

            // Acquisition price
            // e.g ':70E::HOLD//STK+511+00081+DE+20170428|68,5+EUR'
            // e.g ':70E::HOLD//1STK|68,5+EUR'
            m = ACQUISITION_PRICE.matcher(clause);
            if (m.find()) {
                acquisitionPrice = new BigDecimal(m.group(1).replace(",", "."));
            }
        }
    }
}
