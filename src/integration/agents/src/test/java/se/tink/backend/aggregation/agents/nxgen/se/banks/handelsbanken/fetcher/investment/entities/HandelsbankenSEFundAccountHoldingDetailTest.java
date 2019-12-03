package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.HandelsbankenSEFundAccountHoldingDetail;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class HandelsbankenSEFundAccountHoldingDetailTest {
    private static final String holdingResponse =
            "{\n"
                    + "  \"_links\": {\n"
                    + "    \"purchase-context\": {\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=PURCHASE&fundOrFundAccountId=NzM1NDc0Mjcz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "      \"gaScreenName\": \"Savings / Fund holdings / Buy\"\n"
                    + "    },\n"
                    + "    \"swap-context\": {\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=SWAP&fundOrFundAccountId=NzM1NDc0Mjcz&iskAccountNumber=1111111111&iskCustodyAccountNumberSwap=222222222&authToken=***MASKED***\",\n"
                    + "      \"gaScreenName\": \"Savings / Fund holdings / Swap\"\n"
                    + "    },\n"
                    + "    \"sell-context\": {\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=SELL&fundOrFundAccountId=NzM1NDc0Mjcz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "      \"gaScreenName\": \"Savings / Fund holdings / Sell\"\n"
                    + "    },\n"
                    + "    \"change-amount-recurring-saving-context\": {\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=CHANGE_AMOUNT_RECURRING_SAVING&fundOrFundAccountId=NzM1NDc0Mjcz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "      \"gaScreenName\": \"Savings / Fund holdings / Recurring saving / Update\"\n"
                    + "    }\n"
                    + "  },\n"
                    + "  \"links\": [{\n"
                    + "    \"rel\": \"purchase-context\",\n"
                    + "    \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=PURCHASE&fundOrFundAccountId=NzM1NDc0Mjcz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "    \"type\": \"***MASKED***\",\n"
                    + "    \"gaScreenName\": \"Savings / Fund holdings / Buy\"\n"
                    + "  }, {\n"
                    + "    \"rel\": \"sell-context\",\n"
                    + "    \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=SELL&fundOrFundAccountId=NzM1NDc0Mjcz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "    \"type\": \"***MASKED***\",\n"
                    + "    \"gaScreenName\": \"Savings / Fund holdings / Sell\"\n"
                    + "  }, {\n"
                    + "    \"rel\": \"swap-context\",\n"
                    + "    \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=SWAP&fundOrFundAccountId=NzM1NDc0Mjcz&iskAccountNumber=1111111111&iskCustodyAccountNumberSwap=222222222&authToken=***MASKED***\",\n"
                    + "    \"type\": \"***MASKED***\",\n"
                    + "    \"gaScreenName\": \"Savings / Fund holdings / Swap\"\n"
                    + "  }, {\n"
                    + "    \"rel\": \"change-amount-recurring-saving-context\",\n"
                    + "    \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=CHANGE_AMOUNT_RECURRING_SAVING&fundOrFundAccountId=NzM1NDc0Mjcz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "    \"type\": \"***MASKED***\",\n"
                    + "    \"gaScreenName\": \"Savings / Fund holdings / Recurring saving / Update\"\n"
                    + "  }],\n"
                    + "  \"accountFormatted\": \"333 333 333\",\n"
                    + "  \"isin\": \"SE0000624421\",\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"shbFund\": true,\n"
                    + "  \"marketValue\": {\n"
                    + "    \"amount\": 24610.35,\n"
                    + "    \"amountFormatted\": \"24 610,35\",\n"
                    + "    \"unit\": \"SEK\",\n"
                    + "    \"currency\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"purchaseValue\": {\n"
                    + "    \"amount\": 19600.00,\n"
                    + "    \"amountFormatted\": \"19 600,00\",\n"
                    + "    \"unit\": \"SEK\",\n"
                    + "    \"currency\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"averageValueOfCost\": null,\n"
                    + "  \"totalChange\": {\n"
                    + "    \"percentage\": 25.5630,\n"
                    + "    \"percentageFormatted\": \"25,56\",\n"
                    + "    \"amountFormatted\": \"5 010,35\",\n"
                    + "    \"changeDirection\": \"POSITIVE\",\n"
                    + "    \"currency\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"recurringSavings\": [{\n"
                    + "    \"depositAccountId\": \"444444444\",\n"
                    + "    \"depositAccountChosenName\": \"ISK-konto\",\n"
                    + "    \"isSaving\": true,\n"
                    + "    \"dayOfTransfer\": -1,\n"
                    + "    \"dateOfTransfer\": \"2019-12-27\",\n"
                    + "    \"frequencyOfTransfer\": \"ONCE_A_MONTH\",\n"
                    + "    \"nextTransferAmount\": {\n"
                    + "      \"amount\": 400.000,\n"
                    + "      \"amountFormatted\": \"400,00\",\n"
                    + "      \"unit\": \"SEK\",\n"
                    + "      \"currency\": \"SEK\"\n"
                    + "    },\n"
                    + "    \"recurringSavingsText\": \"Månadssparande: 400,00 kr/mån\"\n"
                    + "  }],\n"
                    + "  \"orderSummary\": null,\n"
                    + "  \"fundDetails\": {\n"
                    + "    \"_links\": {\n"
                    + "      \"add-favourite\": {\n"
                    + "        \"href\": \"https://m2.handelsbanken.se/app/priv/market/v2/instruments/userList/instrument/4504%5E0P00000F87?authToken=***MASKED***\"\n"
                    + "      },\n"
                    + "      \"sustainability-profile-pdf\": {\n"
                    + "        \"href\": \"http://www.hallbarhetsprofilen.se/pdf/SE0000624421.pdf\",\n"
                    + "        \"type\": \"***MASKED***\",\n"
                    + "        \"title\": \"Hållbarhetsprofil\"\n"
                    + "      },\n"
                    + "      \"productsheet-pdf\": {\n"
                    + "        \"href\": \"https://secure.msse.se/shb/sv/funds/SE0000624421/kiid.pdf\",\n"
                    + "        \"type\": \"***MASKED***\",\n"
                    + "        \"title\": \"Faktablad\"\n"
                    + "      },\n"
                    + "      \"remove-favourite\": {\n"
                    + "        \"href\": \"https://m2.handelsbanken.se/app/priv/market/v2/instruments/userList/instrument/4504%5E0P00000F87?authToken=***MASKED***\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    \"links\": [{\n"
                    + "      \"rel\": \"add-favourite\",\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/market/v2/instruments/userList/instrument/4504%5E0P00000F87?authToken=***MASKED***\",\n"
                    + "      \"type\": \"***MASKED***\"\n"
                    + "    }, {\n"
                    + "      \"rel\": \"remove-favourite\",\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/market/v2/instruments/userList/instrument/4504%5E0P00000F87?authToken=***MASKED***\",\n"
                    + "      \"type\": \"***MASKED***\"\n"
                    + "    }, {\n"
                    + "      \"rel\": \"productsheet-pdf\",\n"
                    + "      \"href\": \"https://secure.msse.se/shb/sv/funds/SE0000624421/kiid.pdf\",\n"
                    + "      \"type\": \"***MASKED***\",\n"
                    + "      \"title\": \"Faktablad\"\n"
                    + "    }, {\n"
                    + "      \"rel\": \"sustainability-profile-pdf\",\n"
                    + "      \"href\": \"http://www.hallbarhetsprofilen.se/pdf/SE0000624421.pdf\",\n"
                    + "      \"type\": \"***MASKED***\",\n"
                    + "      \"title\": \"Hållbarhetsprofil\"\n"
                    + "    }],\n"
                    + "    \"name\": \"Handelsbanken Europa Index Criteria\",\n"
                    + "    \"externalFundId\": \"0P00000F87\",\n"
                    + "    \"isin\": \"SE0000624421\",\n"
                    + "    \"tradeCurrency\": \"SEK\",\n"
                    + "    \"shortdescription\": \"Handelsbanken Europa Index Criteria är en indexfond och har som mål att så nära som möjligt följa utvecklingen i indexet Solactive ISS ESG Screened Europe. Fonden placerar i aktier utgivna av företag på de utvecklade aktiemarknaderna i Europa. Indexet består av stora och medelstora europeiska bolag på de Europeiska börserna exklusive de bolag som ej uppfyller indexets hållbarhetskrav. Detta är en fond som placerar med särskilda hållbarhetskriterier. Hur vi tillämpar hållbarhetskriterier är beskrivet i fondens Informationsbroschyr.\",\n"
                    + "    \"morningstarrating\": \"4\",\n"
                    + "    \"fundppmcode\": \"Ja (629543)\",\n"
                    + "    \"fundrisk\": \"5\",\n"
                    + "    \"fundriskFormatted\": \"Medel (5/7)\",\n"
                    + "    \"riskText\": \"Risk (1-7) är ett mått på fondens historiska kurssvängningar, om fonden är ny kan risken istället baseras på fondens jämförelseindex. Risknivå 1 står för låg risk och 7 står för hög risk.\",\n"
                    + "    \"instrumentclass\": \"Aktiefonder\",\n"
                    + "    \"instrumentsubclass\": \"Europa\",\n"
                    + "    \"productSheetUrl\": \"https://secure.msse.se/shb/sv/funds/SE0000624421/kiid.pdf\",\n"
                    + "    \"normanAmountText\": \"1 791 SEK\",\n"
                    + "    \"ratingText\": \"Ratingen visar vilka fonder som historiskt sett haft bäst utveckling i sin kategori, i förhållande till risk. Den presenteras i form av antal stjärnor (1-5) där 5 är bäst. För att en fond ska få rating måste den ha minst tre års historik. Ratingen är framtagen av det oberoende fondinformationsföretaget Morningstar.\",\n"
                    + "    \"sustainability\": \"3\",\n"
                    + "    \"sustainabilityProfileText\": \"Hållbarhetsprofilen beskriver hur fonden tillämpar hållbarhetskriterier i förvaltningen. Profilen har utvecklats av branschorganisationen SWESIF tillsammans med dess medlemmar.\",\n"
                    + "    \"sustainabilityRatingText\": \"Hållbarhetsbetyget mäter hur väl de företag som en fond har investerat i hanterar hållbarhetsrisker jämfört med andra fonder inom samma fondkategori. De fonder som analyseras kan få ett Hållbarhetsbetyg på mellan 1 och 5 glober, där 5 är det högsta betyget.\",\n"
                    + "    \"priText\": \"Fonden följer FN:s principer för ansvarsfulla investeringar (Principles for Responsible Investments).\",\n"
                    + "    \"managementFeeText\": \"0,2%\",\n"
                    + "    \"managementFeeDiscountText\": null,\n"
                    + "    \"subscriptionFeeText\": \"0%\",\n"
                    + "    \"withdrawalFeeText\": \"0%\",\n"
                    + "    \"manager\": \"Christian Sopov\",\n"
                    + "    \"managerComment\": \"\",\n"
                    + "    \"managerCommentDate\": \"\",\n"
                    + "    \"navAmountText\": \"135,04 SEK (2019-11-26)\",\n"
                    + "    \"disclaimerText\": \"Fonder graderas i sju riskklasser, 1 för lägst och 7 för högst risk.\\n\\nHistorisk avkastning är ingen garanti för framtida avkastning. De pengar som placeras i fonder kan både öka och minska i värde och det är inte säkert att du får tillbaka hela det insatta beloppet.\\n\\nInformationsbroschyr och faktablad finns på www.handelsbanken.se/fonder.\\n\\n© Svenska Handelsbanken AB (publ)\",\n"
                    + "    \"internal\": true,\n"
                    + "    \"periodChanges\": [{\n"
                    + "      \"timePeriod\": \"DAY1\",\n"
                    + "      \"timePeriodLabel\": \"1 dag\",\n"
                    + "      \"diffPercentage\": -0.125730300,\n"
                    + "      \"diffPercentageFormatted\": \"-0,13\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"MONTH1\",\n"
                    + "      \"timePeriodLabel\": \"1 mån\",\n"
                    + "      \"diffPercentage\": 1.748041000,\n"
                    + "      \"diffPercentageFormatted\": \"1,75\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"MONTH3\",\n"
                    + "      \"timePeriodLabel\": \"3 mån\",\n"
                    + "      \"diffPercentage\": 9.450478200,\n"
                    + "      \"diffPercentageFormatted\": \"9,45\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YTD\",\n"
                    + "      \"timePeriodLabel\": \"2019\",\n"
                    + "      \"diffPercentage\": 30.046225000,\n"
                    + "      \"diffPercentageFormatted\": \"30,05\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR1\",\n"
                    + "      \"timePeriodLabel\": \"1 år\",\n"
                    + "      \"diffPercentage\": 21.427929100,\n"
                    + "      \"diffPercentageFormatted\": \"21,43\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR3\",\n"
                    + "      \"timePeriodLabel\": \"3 år\",\n"
                    + "      \"diffPercentage\": 38.872891800,\n"
                    + "      \"diffPercentageFormatted\": \"38,87\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR5\",\n"
                    + "      \"timePeriodLabel\": \"5 år\",\n"
                    + "      \"diffPercentage\": 51.186744300,\n"
                    + "      \"diffPercentageFormatted\": \"51,19\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR10\",\n"
                    + "      \"timePeriodLabel\": \"10 år\",\n"
                    + "      \"diffPercentage\": 115.110854300,\n"
                    + "      \"diffPercentageFormatted\": \"115,11\"\n"
                    + "    }],\n"
                    + "    \"graphUrlList\": [{\n"
                    + "      \"timePeriod\": \"YEAR1\",\n"
                    + "      \"timePeriodLabel\": \"1 år\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F87&cs=ci:sv-SE;cur:SEK;start:2018-11-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YTD\",\n"
                    + "      \"timePeriodLabel\": \"2019\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F87&cs=ci:sv-SE;cur:SEK;start:2018-12-31;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"MONTH1\",\n"
                    + "      \"timePeriodLabel\": \"1 mån\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F87&cs=ci:sv-SE;cur:SEK;start:2019-10-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR3\",\n"
                    + "      \"timePeriodLabel\": \"3 år\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F87&cs=ci:sv-SE;cur:SEK;start:2016-11-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR5\",\n"
                    + "      \"timePeriodLabel\": \"5 år\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F87&cs=ci:sv-SE;cur:SEK;start:2014-11-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR10\",\n"
                    + "      \"timePeriodLabel\": \"10 år\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F87&cs=ci:sv-SE;cur:SEK;start:2009-11-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }],\n"
                    + "    \"favouriteStatus\": \"UNSELECTED\"\n"
                    + "  }\n"
                    + "}";

    private static final String holdingResponse2 =
            "{\n"
                    + "  \"_links\": {\n"
                    + "    \"purchase-context\": {\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=PURCHASE&fundOrFundAccountId=NzM1NDcyMDMz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "      \"gaScreenName\": \"Savings / Fund holdings / Buy\"\n"
                    + "    },\n"
                    + "    \"swap-context\": {\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=SWAP&fundOrFundAccountId=NzM1NDcyMDMz&iskAccountNumber=1111111111&iskCustodyAccountNumberSwap=222222222&authToken=***MASKED***\",\n"
                    + "      \"gaScreenName\": \"Savings / Fund holdings / Swap\"\n"
                    + "    },\n"
                    + "    \"sell-context\": {\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=SELL&fundOrFundAccountId=NzM1NDcyMDMz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "      \"gaScreenName\": \"Savings / Fund holdings / Sell\"\n"
                    + "    },\n"
                    + "    \"change-amount-recurring-saving-context\": {\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=CHANGE_AMOUNT_RECURRING_SAVING&fundOrFundAccountId=NzM1NDcyMDMz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "      \"gaScreenName\": \"Savings / Fund holdings / Recurring saving / Update\"\n"
                    + "    }\n"
                    + "  },\n"
                    + "  \"links\": [{\n"
                    + "    \"rel\": \"purchase-context\",\n"
                    + "    \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=PURCHASE&fundOrFundAccountId=NzM1NDcyMDMz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "    \"type\": \"***MASKED***\",\n"
                    + "    \"gaScreenName\": \"Savings / Fund holdings / Buy\"\n"
                    + "  }, {\n"
                    + "    \"rel\": \"sell-context\",\n"
                    + "    \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=SELL&fundOrFundAccountId=NzM1NDcyMDMz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "    \"type\": \"***MASKED***\",\n"
                    + "    \"gaScreenName\": \"Savings / Fund holdings / Sell\"\n"
                    + "  }, {\n"
                    + "    \"rel\": \"swap-context\",\n"
                    + "    \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=SWAP&fundOrFundAccountId=NzM1NDcyMDMz&iskAccountNumber=1111111111&iskCustodyAccountNumberSwap=222222222&authToken=***MASKED***\",\n"
                    + "    \"type\": \"***MASKED***\",\n"
                    + "    \"gaScreenName\": \"Savings / Fund holdings / Swap\"\n"
                    + "  }, {\n"
                    + "    \"rel\": \"change-amount-recurring-saving-context\",\n"
                    + "    \"href\": \"https://m2.handelsbanken.se/app/priv/fund/orderContext?contextType=CHANGE_AMOUNT_RECURRING_SAVING&fundOrFundAccountId=NzM1NDcyMDMz&iskAccountNumber=1111111111&authToken=***MASKED***\",\n"
                    + "    \"type\": \"***MASKED***\",\n"
                    + "    \"gaScreenName\": \"Savings / Fund holdings / Recurring saving / Update\"\n"
                    + "  }],\n"
                    + "  \"accountFormatted\": \"735 472 033\",\n"
                    + "  \"isin\": \"SE0000582025\",\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"shbFund\": true,\n"
                    + "  \"marketValue\": {\n"
                    + "    \"amount\": 6200.60,\n"
                    + "    \"amountFormatted\": \"6 200,60\",\n"
                    + "    \"unit\": \"SEK\",\n"
                    + "    \"currency\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"purchaseValue\": {\n"
                    + "    \"amount\": 5517.18,\n"
                    + "    \"amountFormatted\": \"5 517,18\",\n"
                    + "    \"unit\": \"SEK\",\n"
                    + "    \"currency\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"averageValueOfCost\": null,\n"
                    + "  \"totalChange\": {\n"
                    + "    \"percentage\": 12.3871,\n"
                    + "    \"percentageFormatted\": \"12,39\",\n"
                    + "    \"amountFormatted\": \"683,42\",\n"
                    + "    \"changeDirection\": \"POSITIVE\",\n"
                    + "    \"currency\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"recurringSavings\": [{\n"
                    + "    \"depositAccountId\": \"444444444\",\n"
                    + "    \"depositAccountChosenName\": \"ISK-konto\",\n"
                    + "    \"isSaving\": true,\n"
                    + "    \"dayOfTransfer\": -1,\n"
                    + "    \"dateOfTransfer\": \"2019-12-27\",\n"
                    + "    \"frequencyOfTransfer\": \"ONCE_A_MONTH\",\n"
                    + "    \"nextTransferAmount\": {\n"
                    + "      \"amount\": 1000.000,\n"
                    + "      \"amountFormatted\": \"1 000,00\",\n"
                    + "      \"unit\": \"SEK\",\n"
                    + "      \"currency\": \"SEK\"\n"
                    + "    },\n"
                    + "    \"recurringSavingsText\": \"Månadssparande: 1 000,00 kr/mån\"\n"
                    + "  }],\n"
                    + "  \"orderSummary\": null,\n"
                    + "  \"fundDetails\": {\n"
                    + "    \"_links\": {\n"
                    + "      \"add-favourite\": {\n"
                    + "        \"href\": \"https://m2.handelsbanken.se/app/priv/market/v2/instruments/userList/instrument/4504%5E0P00000F81?authToken=***MASKED***\"\n"
                    + "      },\n"
                    + "      \"sustainability-profile-pdf\": {\n"
                    + "        \"href\": \"http://www.hallbarhetsprofilen.se/pdf/SE0000582025.pdf\",\n"
                    + "        \"type\": \"***MASKED***\",\n"
                    + "        \"title\": \"Hållbarhetsprofil\"\n"
                    + "      },\n"
                    + "      \"productsheet-pdf\": {\n"
                    + "        \"href\": \"https://secure.msse.se/shb/sv/funds/SE0000582025/kiid.pdf\",\n"
                    + "        \"type\": \"***MASKED***\",\n"
                    + "        \"title\": \"Faktablad\"\n"
                    + "      },\n"
                    + "      \"remove-favourite\": {\n"
                    + "        \"href\": \"https://m2.handelsbanken.se/app/priv/market/v2/instruments/userList/instrument/4504%5E0P00000F81?authToken=***MASKED***\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    \"links\": [{\n"
                    + "      \"rel\": \"add-favourite\",\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/market/v2/instruments/userList/instrument/4504%5E0P00000F81?authToken=***MASKED***\",\n"
                    + "      \"type\": \"***MASKED***\"\n"
                    + "    }, {\n"
                    + "      \"rel\": \"remove-favourite\",\n"
                    + "      \"href\": \"https://m2.handelsbanken.se/app/priv/market/v2/instruments/userList/instrument/4504%5E0P00000F81?authToken=***MASKED***\",\n"
                    + "      \"type\": \"***MASKED***\"\n"
                    + "    }, {\n"
                    + "      \"rel\": \"productsheet-pdf\",\n"
                    + "      \"href\": \"https://secure.msse.se/shb/sv/funds/SE0000582025/kiid.pdf\",\n"
                    + "      \"type\": \"***MASKED***\",\n"
                    + "      \"title\": \"Faktablad\"\n"
                    + "    }, {\n"
                    + "      \"rel\": \"sustainability-profile-pdf\",\n"
                    + "      \"href\": \"http://www.hallbarhetsprofilen.se/pdf/SE0000582025.pdf\",\n"
                    + "      \"type\": \"***MASKED***\",\n"
                    + "      \"title\": \"Hållbarhetsprofil\"\n"
                    + "    }],\n"
                    + "    \"name\": \"Handelsbanken Sverigefond Index\",\n"
                    + "    \"externalFundId\": \"0P00000F81\",\n"
                    + "    \"isin\": \"SE0000582025\",\n"
                    + "    \"tradeCurrency\": \"SEK\",\n"
                    + "    \"shortdescription\": \"Sverigefond Index placerar på den svenska aktiemarknaden och har som mål att följa ett index som består av aktier som handlas på NASDAQ OMX Nordic Exchange (Stockholmsbörsen). \",\n"
                    + "    \"morningstarrating\": \"3\",\n"
                    + "    \"fundppmcode\": \"Nej\",\n"
                    + "    \"fundrisk\": \"5\",\n"
                    + "    \"fundriskFormatted\": \"Medel (5/7)\",\n"
                    + "    \"riskText\": \"Risk (1-7) är ett mått på fondens historiska kurssvängningar, om fonden är ny kan risken istället baseras på fondens jämförelseindex. Risknivå 1 står för låg risk och 7 står för hög risk.\",\n"
                    + "    \"instrumentclass\": \"Aktiefonder\",\n"
                    + "    \"instrumentsubclass\": \"Sverige/Norden\",\n"
                    + "    \"productSheetUrl\": \"https://secure.msse.se/shb/sv/funds/SE0000582025/kiid.pdf\",\n"
                    + "    \"normanAmountText\": \"5 990 SEK\",\n"
                    + "    \"ratingText\": \"Ratingen visar vilka fonder som historiskt sett haft bäst utveckling i sin kategori, i förhållande till risk. Den presenteras i form av antal stjärnor (1-5) där 5 är bäst. För att en fond ska få rating måste den ha minst tre års historik. Ratingen är framtagen av det oberoende fondinformationsföretaget Morningstar.\",\n"
                    + "    \"sustainability\": \"4\",\n"
                    + "    \"sustainabilityProfileText\": \"Hållbarhetsprofilen beskriver hur fonden tillämpar hållbarhetskriterier i förvaltningen. Profilen har utvecklats av branschorganisationen SWESIF tillsammans med dess medlemmar.\",\n"
                    + "    \"sustainabilityRatingText\": \"Hållbarhetsbetyget mäter hur väl de företag som en fond har investerat i hanterar hållbarhetsrisker jämfört med andra fonder inom samma fondkategori. De fonder som analyseras kan få ett Hållbarhetsbetyg på mellan 1 och 5 glober, där 5 är det högsta betyget.\",\n"
                    + "    \"priText\": \"Fonden följer FN:s principer för ansvarsfulla investeringar (Principles for Responsible Investments).\",\n"
                    + "    \"managementFeeText\": \"0,65%\",\n"
                    + "    \"managementFeeDiscountText\": null,\n"
                    + "    \"subscriptionFeeText\": \"0%\",\n"
                    + "    \"withdrawalFeeText\": \"0%\",\n"
                    + "    \"manager\": \"Anders Dolata\",\n"
                    + "    \"managerComment\": \"\",\n"
                    + "    \"managerCommentDate\": \"\",\n"
                    + "    \"navAmountText\": \"13 034,69 SEK (2019-11-26)\",\n"
                    + "    \"disclaimerText\": \"Fonder graderas i sju riskklasser, 1 för lägst och 7 för högst risk.\\n\\nHistorisk avkastning är ingen garanti för framtida avkastning. De pengar som placeras i fonder kan både öka och minska i värde och det är inte säkert att du får tillbaka hela det insatta beloppet.\\n\\nInformationsbroschyr och faktablad finns på www.handelsbanken.se/fonder.\\n\\n© Svenska Handelsbanken AB (publ)\",\n"
                    + "    \"internal\": true,\n"
                    + "    \"periodChanges\": [{\n"
                    + "      \"timePeriod\": \"DAY1\",\n"
                    + "      \"timePeriodLabel\": \"1 dag\",\n"
                    + "      \"diffPercentage\": 0.344806300,\n"
                    + "      \"diffPercentageFormatted\": \"0,34\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"MONTH1\",\n"
                    + "      \"timePeriodLabel\": \"1 mån\",\n"
                    + "      \"diffPercentage\": 2.018503900,\n"
                    + "      \"diffPercentageFormatted\": \"2,02\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"MONTH3\",\n"
                    + "      \"timePeriodLabel\": \"3 mån\",\n"
                    + "      \"diffPercentage\": 13.024141900,\n"
                    + "      \"diffPercentageFormatted\": \"13,02\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YTD\",\n"
                    + "      \"timePeriodLabel\": \"2019\",\n"
                    + "      \"diffPercentage\": 29.811427900,\n"
                    + "      \"diffPercentageFormatted\": \"29,81\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR1\",\n"
                    + "      \"timePeriodLabel\": \"1 år\",\n"
                    + "      \"diffPercentage\": 24.255876900,\n"
                    + "      \"diffPercentageFormatted\": \"24,26\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR3\",\n"
                    + "      \"timePeriodLabel\": \"3 år\",\n"
                    + "      \"diffPercentage\": 38.568295500,\n"
                    + "      \"diffPercentageFormatted\": \"38,57\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR5\",\n"
                    + "      \"timePeriodLabel\": \"5 år\",\n"
                    + "      \"diffPercentage\": 63.446438200,\n"
                    + "      \"diffPercentageFormatted\": \"63,45\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR10\",\n"
                    + "      \"timePeriodLabel\": \"10 år\",\n"
                    + "      \"diffPercentage\": 197.061519700,\n"
                    + "      \"diffPercentageFormatted\": \"197,06\"\n"
                    + "    }],\n"
                    + "    \"graphUrlList\": [{\n"
                    + "      \"timePeriod\": \"YEAR1\",\n"
                    + "      \"timePeriodLabel\": \"1 år\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F81&cs=ci:sv-SE;cur:SEK;start:2018-11-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YTD\",\n"
                    + "      \"timePeriodLabel\": \"2019\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F81&cs=ci:sv-SE;cur:SEK;start:2018-12-31;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"MONTH1\",\n"
                    + "      \"timePeriodLabel\": \"1 mån\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F81&cs=ci:sv-SE;cur:SEK;start:2019-10-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR3\",\n"
                    + "      \"timePeriodLabel\": \"3 år\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F81&cs=ci:sv-SE;cur:SEK;start:2016-11-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR5\",\n"
                    + "      \"timePeriodLabel\": \"5 år\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F81&cs=ci:sv-SE;cur:SEK;start:2014-11-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }, {\n"
                    + "      \"timePeriod\": \"YEAR10\",\n"
                    + "      \"timePeriodLabel\": \"10 år\",\n"
                    + "      \"url\": \"https://m2.handelsbanken.se/app/priv/market/funds/graph?authToken=***MASKED***&f=0P00000F81&cs=ci:sv-SE;cur:SEK;start:2009-11-26;end:2019-11-26;fontsize:{fs};linewidth:{lw}&w={w}&h={h}\"\n"
                    + "    }],\n"
                    + "    \"favouriteStatus\": \"UNSELECTED\"\n"
                    + "  }\n"
                    + "}";

    @Test
    public void testFundDetails() {
        HandelsbankenSEFundAccountHoldingDetail details =
                SerializationUtils.deserializeFromString(
                        holdingResponse, HandelsbankenSEFundAccountHoldingDetail.class);
        Optional<Instrument> instrument = details.toInstrument();
        assertTrue(instrument.isPresent());
        assertEquals(new Double(182.2448904028436), instrument.get().getQuantity());
    }

    @Test
    public void testFundDetails2() {
        HandelsbankenSEFundAccountHoldingDetail details =
                SerializationUtils.deserializeFromString(
                        holdingResponse2, HandelsbankenSEFundAccountHoldingDetail.class);
        Optional<Instrument> instrument = details.toInstrument();
        assertTrue(instrument.isPresent());
        assertEquals(new Double(0.47569984403158033), instrument.get().getQuantity());
    }
}
