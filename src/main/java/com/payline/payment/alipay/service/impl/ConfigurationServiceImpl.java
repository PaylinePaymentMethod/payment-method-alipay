package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.bean.configuration.Acquirer;
import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.bean.request.SingleTradeQuery;
import com.payline.payment.alipay.bean.response.APIResponse;
import com.payline.payment.alipay.enumeration.PartnerTransactionIdOptions;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.service.AcquirerService;
import com.payline.payment.alipay.utils.PluginUtils;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.payment.alipay.utils.http.HttpClient;
import com.payline.payment.alipay.utils.i18n.I18nService;
import com.payline.payment.alipay.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.configuration.request.ContractParametersRequest;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.ConfigurationService;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.payline.payment.alipay.bean.object.ForexService.SINGLE_TRADE_QUERY;
import static com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest.GENERIC_ERROR;

public class ConfigurationServiceImpl implements ConfigurationService {
    private static final Logger LOGGER = LogManager.getLogger(ConfigurationServiceImpl.class);

    private static final String I18N_CONTRACT_PREFIX = "contract.";
    private I18nService i18n = I18nService.getInstance();
    private ReleaseProperties releaseProperties = ReleaseProperties.getInstance();
    private HttpClient client = HttpClient.getInstance();
    private AcquirerService acquirerService = AcquirerService.getInstance();

    @Override
    public List<AbstractParameter> getParameters(final Locale locale) {
        throw new IllegalStateException("Method not allowed");
    }

    @Override
    public List<AbstractParameter> getParameters(final ContractParametersRequest request) {
        final Locale locale = request.getLocale();
        List<AbstractParameter> parameters = new ArrayList<>();

        // ACQUIRER
        final List<Acquirer> acquirers = acquirerService.retrieveAcquirers(request.getPluginConfiguration());
        final ListBoxParameter acquirerListBoxParameter = new ListBoxParameter();
        acquirerListBoxParameter.setKey(ContractConfigurationKeys.ACQUIRER_ID);
        acquirerListBoxParameter.setLabel(i18n.getMessage("contract.acquirer.label", locale));
        acquirerListBoxParameter.setDescription(i18n.getMessage("contract.acquirer.description", locale));
        final Map<String, String> acquirerMap = new HashMap<>();
        for (final Acquirer acquirer : acquirers) {
            acquirerMap.put(acquirer.getId(), acquirer.getLabel());
        }
        acquirerListBoxParameter.setList(acquirerMap);
        acquirerListBoxParameter.setRequired(true);
        parameters.add(acquirerListBoxParameter);

        // SUPPLIER
        InputParameter supplier = new InputParameter();
        supplier.setKey(ContractConfigurationKeys.SUPPLIER);
        supplier.setLabel(i18n.getMessage("contract.SUPPLIER.label", locale));
        supplier.setDescription(i18n.getMessage("contract.SUPPLIER.description", locale));
        supplier.setRequired(true);
        parameters.add(supplier);

        // SECONDARY_MERCHANT_ID
        InputParameter secondaryMerchantId = new InputParameter();
        secondaryMerchantId.setKey(ContractConfigurationKeys.SECONDARY_MERCHANT_ID);
        secondaryMerchantId.setLabel(i18n.getMessage("contract.SECONDARY_MERCHANT_ID.label", locale));
        secondaryMerchantId.setDescription(i18n.getMessage("contract.SECONDARY_MERCHANT_ID.description", locale));
        secondaryMerchantId.setRequired(true);
        parameters.add(secondaryMerchantId);

        // SECONDARY_MERCHANT_NAME
        InputParameter secondaryMerchantName = new InputParameter();
        secondaryMerchantName.setKey(ContractConfigurationKeys.SECONDARY_MERCHANT_NAME);
        secondaryMerchantName.setLabel(i18n.getMessage("contract.SECONDARY_MERCHANT_NAME.label", locale));
        secondaryMerchantName.setDescription(i18n.getMessage("contract.SECONDARY_MERCHANT_NAME.description", locale));
        secondaryMerchantName.setRequired(true);
        parameters.add(secondaryMerchantName);

        // SECONDARY_MERCHANT_INDUSTRY
        ListBoxParameter secondaryMerchantIndustry = new ListBoxParameter();
        secondaryMerchantIndustry.setKey(ContractConfigurationKeys.SECONDARY_MERCHANT_INDUSTRY);
        secondaryMerchantIndustry.setLabel(i18n.getMessage("contract.SECONDARY_MERCHANT_INDUSTRY.label", locale));
        secondaryMerchantIndustry.setDescription(i18n.getMessage("contract.SECONDARY_MERCHANT_INDUSTRY.description", locale));
        secondaryMerchantIndustry.setList(this.createMMC());
        secondaryMerchantIndustry.setRequired(true);
        parameters.add(secondaryMerchantIndustry);

        // MERCHANT_URL
        InputParameter merchantUrl = new InputParameter();
        merchantUrl.setKey(ContractConfigurationKeys.MERCHANT_URL);
        merchantUrl.setLabel(i18n.getMessage("contract.MERCHANT_URL.label", locale));
        merchantUrl.setDescription(i18n.getMessage("contract.MERCHANT_URL.description", locale));
        merchantUrl.setRequired(false);
        parameters.add(merchantUrl);

        // merchant bank code
        InputParameter merchantBankCode = new InputParameter();
        merchantBankCode.setKey(ContractConfigurationKeys.MERCHANT_BANK_CODE);
        merchantBankCode.setLabel(i18n.getMessage("contract.MERCHANT_BANK_CODE.label", locale));
        merchantBankCode.setDescription(i18n.getMessage("contract.MERCHANT_BANK_CODE.description", locale));
        merchantBankCode.setRequired(true);
        parameters.add(merchantBankCode);


        final ListBoxParameter partnerTransactionIdListBoxParameter = new ListBoxParameter();
        partnerTransactionIdListBoxParameter.setKey(ContractConfigurationKeys.PARTNER_TRANSACTION_ID);
        partnerTransactionIdListBoxParameter.setLabel(i18n.getMessage("contract.partnerTransactionId.label", locale));
        partnerTransactionIdListBoxParameter.setDescription(i18n.getMessage("contract.partnerTransactionId.description", locale));
        final Map<String, String> partnerTransactionIdMap = new HashMap<>();
        for (final PartnerTransactionIdOptions partnerTransactionIdOption : PartnerTransactionIdOptions.values()) {
            partnerTransactionIdMap.put(partnerTransactionIdOption.name(), i18n.getMessage(partnerTransactionIdOption.getI18nKey(), locale));
        }
        partnerTransactionIdListBoxParameter.setList(partnerTransactionIdMap);
        partnerTransactionIdListBoxParameter.setRequired(true);
        parameters.add(partnerTransactionIdListBoxParameter);
        return parameters;
    }


    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {
        RequestConfiguration configuration = RequestConfiguration.build(contractParametersCheckRequest);
        final Map<String, String> errors = new HashMap<>();

        Map<String, String> accountInfo = contractParametersCheckRequest.getAccountInfo();
        Locale locale = contractParametersCheckRequest.getLocale();

        // check required fields
        final ContractParametersRequest contractParametersRequest = ContractParametersRequest.builder()
                .pluginConfiguration(contractParametersCheckRequest.getPluginConfiguration())
                .locale(locale)
                .build();
        for (AbstractParameter param : this.getParameters(contractParametersRequest)) {
            if (param.isRequired() && PluginUtils.isEmpty(accountInfo.get(param.getKey()))) {
                String message = i18n.getMessage(I18N_CONTRACT_PREFIX + param.getKey() + ".requiredError", locale);
                errors.put(param.getKey(), message);
            }
        }

        // If partner id is missing, no need to go further, as it is required
        if (!errors.isEmpty()) {
            return errors;
        }

        // check the length of some fields

        if (accountInfo.get(ContractConfigurationKeys.MERCHANT_BANK_CODE).length() != 5) {
            errors.put(ContractConfigurationKeys.MERCHANT_BANK_CODE, i18n.getMessage("contract.MERCHANT_BANK_CODE.badLength", locale));
        }

        try {

            final String merchantPID = acquirerService.retrieveAcquirer(contractParametersCheckRequest.getPluginConfiguration(), accountInfo.get(ContractConfigurationKeys.ACQUIRER_ID)).getMerchantPID();
            // create a fake single_trade_query request object with a wrong transactionId
            SingleTradeQuery singleTradeQuery = SingleTradeQuery.SingleTradeQueryBuilder
                    .aSingleTradeQuery()
                    .withOutTradeNo("0")
                    .withPartner(merchantPID)
                    .withService(SINGLE_TRADE_QUERY)
                    .build();

            // call get API
            APIResponse response = client.get(configuration, singleTradeQuery.getParametersList());

            // response should not be successful
            if ("ILLEGAL_PARTNER".equalsIgnoreCase(response.getError())) {
                errors.put(ContractConfigurationKeys.ACQUIRER_ID, response.getError());
            } else if (!"TRADE_NOT_EXIST".equalsIgnoreCase(response.getError())) {
                errors.put(GENERIC_ERROR, response.getError());
            }
        } catch (PluginException e) {
            errors.put(GENERIC_ERROR, e.getErrorCode());
        } catch (RuntimeException e) {
            errors.put(GENERIC_ERROR, e.getMessage());
        }
        return errors;
    }


    @Override
    public ReleaseInformation getReleaseInformation() {
        String date = releaseProperties.get("release.date");
        String version = releaseProperties.get("release.version");

        if (PluginUtils.isEmpty(date)) {
            LOGGER.error("Date is not defined");
            throw new PluginException("Plugin error: Date is not defined");
        }

        if (PluginUtils.isEmpty(version)) {
            LOGGER.error("Version is not defined");
            throw new PluginException("Plugin error: Version is not defined");
        }

        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .withVersion(version)
                .build();
    }

    @Override
    public String getName(Locale locale) {
        return i18n.getMessage("paymentMethod.name", locale);
    }

    /**
     * Create the list of MCC
     *
     * @return MCC list provided by Alipay at https://global.alipay.com/doc/online/mcc
     */
    private Map<String, String> createMMC() {
        Map<String, String> mccs = new HashMap<>();
        mccs.put("0742", "Veterinary services");
        mccs.put("0743", "Wine producers");
        mccs.put("0744", "Champagne producers");
        mccs.put("0780", "Landscaping and horticultural services");
        mccs.put("1711", "Heating, plumbing and air-conditioning contractors");
        mccs.put("1731", "Electrical contractors");
        mccs.put("1740", "Masonry, stonework, tile setting, plastering and insulation contractors");
        mccs.put("1750", "Carpentry contractors");
        mccs.put("1761", "Roofing, siding and sheet metal work contractors");
        mccs.put("1771", "Concrete work contractors");
        mccs.put("1799", "Special trade contractors - not elsewhere classified");
        mccs.put("2741", "Miscellaneous publishing and printing services");
        mccs.put("2791", "Typesetting, platemaking and related services");
        mccs.put("2842", "Speciality cleaning, polishing and sanitation preparations");
        mccs.put("4011", "Railroads");
        mccs.put("4111", "Local and suburban commuter passenger transportation, including ferries");
        mccs.put("4112", "Passenger railways");
        mccs.put("4119", "Ambulance services");
        mccs.put("4121", "Taxi-cabs and limousines");
        mccs.put("4131", "Bus lines");
        mccs.put("4214", "Motor freight carriers and trucking - local and long distance, moving and storage companies and local delivery");
        mccs.put("4215", "Courier services - air and ground and freight forwarders");
        mccs.put("4225", "Public warehousing and storage - farm products, refrigerated goods and household goods");
        mccs.put("4411", "Steamships and cruise lines");
        mccs.put("4457", "Boat rentals and leasing");
        mccs.put("4468", "Marinas, marine service and supplies");
        mccs.put("4511", "Airlines and air carriers");
        mccs.put("4582", "Airports, flying fields and airport terminals");
        mccs.put("4722", "Travel agencies and tour operators");
        mccs.put("4784", "Tolls and bridge fees");
        mccs.put("4789", "Transportation services - not elsewhere classified");
        mccs.put("4812", "Telecommunication equipment and telephone sales");
        mccs.put("4814", "Telecommunication services");
        mccs.put("4815", "Monthly summary telephone charges");
        mccs.put("4816", "Computer network information services");
        mccs.put("4821", "Telegraph services");
        mccs.put("4899", "Cable and other pay television services");
        mccs.put("4900", "Utilities - electric, gas, water and sanitary");
        mccs.put("5013", "Motor vehicle supplies and new parts");
        mccs.put("5021", "Office and commercial furniture");
        mccs.put("5039", "Construction materials - not elsewhere classified");
        mccs.put("5044", "Office, photographic, photocopy and microfilm equipment");
        mccs.put("5045", "Computers, computer peripheral equipment - not elsewhere classified");
        mccs.put("5046", "Commercial equipment - not elsewhere classified");
        mccs.put("5047", "Dental laboratory medical ophthalmic hospital equipment and supplies");
        mccs.put("5051", "Metal service centres and offices");
        mccs.put("5065", "Electrical parts and equipment");
        mccs.put("5072", "Hardware equipment and supplies");
        mccs.put("5074", "Plumbing and heating equipment and supplies");
        mccs.put("5085", "Industrial supplies - not elsewhere classified");
        mccs.put("5094", "Precious stones and metals, watches and jewellery");
        mccs.put("5099", "Durable goods - not elsewhere classified");
        mccs.put("5111", "Stationery, office supplies and printing and writing paper");
        mccs.put("5122", "Drugs, drug proprietors");
        mccs.put("5131", "Piece goods, notions and other dry goods");
        mccs.put("5137", "Men’s, women’s and children’s uniforms and commercial clothing");
        mccs.put("5139", "Commercial footwear");
        mccs.put("5169", "Chemicals and allied products - not elsewhere classified");
        mccs.put("5192", "Books, periodicals and newspapers");
        mccs.put("5193", "Florists’ supplies, nursery stock and flowers");
        mccs.put("5198", "Paints, varnishes and supplies");
        mccs.put("5199", "Non-durable goods - not elsewhere classified");
        mccs.put("5200", "Home supply warehouse outlets");
        mccs.put("5211", "Lumber and building materials outlets");
        mccs.put("5231", "Glass, paint and wallpaper shops");
        mccs.put("5251", "Hardware shops");
        mccs.put("5261", "Lawn and garden supplies outlets, including nurseries");
        mccs.put("5271", "Mobile home dealers");
        mccs.put("5300", "Wholesale clubs");
        mccs.put("5309", "Duty-free shops");
        mccs.put("5310", "Discount shops");
        mccs.put("5311", "Department stores");
        mccs.put("5331", "Variety stores");
        mccs.put("5399", "Miscellaneous general merchandise");
        mccs.put("5411", "Groceries and supermarkets");
        mccs.put("5422", "Freezer and locker meat provisioners");
        mccs.put("5441", "Candy, nut and confectionery shops");
        mccs.put("5451", "Dairies");
        mccs.put("5462", "Bakeries");
        mccs.put("5499", "Miscellaneous food shops - convenience and speciality retail outlets");
        mccs.put("5511", "Car and truck dealers (new and used) sales, services, repairs, parts and leasing");
        mccs.put("5521", "Car and truck dealers (used only) sales, service, repairs, parts and leasing");
        mccs.put("5531", "Auto and home supply outlets");
        mccs.put("5532", "Automotive tyre outlets");
        mccs.put("5533", "Automotive parts and accessories outlets");
        mccs.put("5541", "Service stations (with or without ancillary services)");
        mccs.put("5542", "Automated fuel dispensers");
        mccs.put("5551", "Boat dealers");
        mccs.put("5561", "Camper, recreational and utility trailer dealers");
        mccs.put("5571", "Motorcycle shops and dealers");
        mccs.put("5592", "Motor home dealers");
        mccs.put("5598", "Snowmobile dealers");
        mccs.put("5599", "Miscellaneous automotive, aircraft and farm equipment dealers -not elsewhere classified");
        mccs.put("5611", "Men’s and boys’ clothing and accessory shops");
        mccs.put("5621", "Women’s ready-to-wear shops");
        mccs.put("5631", "Women’s accessory and speciality shops");
        mccs.put("5641", "Children’s and infants’ wear shops");
        mccs.put("5651", "Family clothing shops");
        mccs.put("5655", "Sports and riding apparel shops");
        mccs.put("5661", "Shoe shops");
        mccs.put("5681", "Furriers and fur shops");
        mccs.put("5691", "Men’s and women’s clothing shops");
        mccs.put("5697", "Tailors, seamstresses, mending and alterations");
        mccs.put("5698", "Wig and toupee shops");
        mccs.put("5699", "Miscellaneous apparel and accessory shops");
        mccs.put("5712", "Furniture, home furnishings and equipment shops and manufacturers, except appliances");
        mccs.put("5713", "Floor covering services");
        mccs.put("5714", "Drapery, window covering and upholstery shops");
        mccs.put("5715", "Alcoholic beverage wholesalers");
        mccs.put("5718", "Fireplaces, fireplace screens and accessories shops");
        mccs.put("5719", "Miscellaneous home furnishing speciality shops");
        mccs.put("5722", "Household appliance shops");
        mccs.put("5732", "Electronics shops");
        mccs.put("5733", "Music shops - musical instruments, pianos and sheet music");
        mccs.put("5734", "Computer software outlets");
        mccs.put("5735", "Record shops");
        mccs.put("5811", "Caterers");
        mccs.put("5812", "Eating places and restaurants");
        mccs.put("5813", "Drinking places (alcoholic beverages) - bars, taverns, night-clubs, cocktail lounges and discothèques");
        mccs.put("5814", "Fast food restaurants");
        mccs.put("5815", "Digital Goods-Media, Books, Movies, Music");
        mccs.put("5816", "Digital Goods-Games");
        mccs.put("5817", "Digital Goods-Software Applications (excluding games)");
        mccs.put("5818", "Digital Goods-Multi-Category");
        mccs.put("5912", "Drug stores and pharmacies");
        mccs.put("5921", "Package shops - beer, wine and liquor");
        mccs.put("5931", "Used merchandise and second-hand shops");
        mccs.put("5935", "Wrecking and salvage yards");
        mccs.put("5937", "Antique reproduction shops");
        mccs.put("5940", "Bicycle shops - sales and service");
        mccs.put("5941", "Sporting goods shops");
        mccs.put("5942", "Bookshops");
        mccs.put("5943", "Stationery, office and school supply shops");
        mccs.put("5944", "Jewellery, watch, clock and silverware shops");
        mccs.put("5945", "Hobby, toy and game shops");
        mccs.put("5946", "Camera and photographic supply shops");
        mccs.put("5947", "Gift, card, novelty and souvenir shops");
        mccs.put("5948", "Luggage and leather goods shops");
        mccs.put("5949", "Sewing, needlework, fabric and piece goods shops");
        mccs.put("5950", "Glassware and crystal shops");
        mccs.put("5962", "Telemarketing - travel-related arrangement services");
        mccs.put("5963", "Door-to-door sales");
        mccs.put("5964", "Direct marketing - catalogue merchants");
        mccs.put("5965", "Direct marketing - combination catalogue and retail merchants");
        mccs.put("5966", "Direct marketing - outbound telemarketing merchants");
        mccs.put("5967", "Direct marketing - inbound telemarketing merchants");
        mccs.put("5968", "Direct marketing - continuity subscription merchants");
        mccs.put("5969", "Direct marketing direct marketers - not elsewhere classified");
        mccs.put("5970", "Artist supply and craft shops");
        mccs.put("5971", "Art dealers and galleries");
        mccs.put("5972", "Stamp and coin shops");
        mccs.put("5973", "Religious goods and shops");
        mccs.put("5975", "Hearing aids - sales, service and supplies");
        mccs.put("5976", "Orthopaedic goods and prosthetic devices");
        mccs.put("5977", "Cosmetic shops");
        mccs.put("5978", "Typewriter outlets - sales, service and rentals");
        mccs.put("5983", "Fuel dealers - fuel oil, wood, coal and liquefied petroleum");
        mccs.put("5992", "Florists");
        mccs.put("5993", "Cigar shops and stands");
        mccs.put("5994", "Newsagents and news-stands");
        mccs.put("5995", "Pet shops, pet food and supplies");
        mccs.put("5996", "Swimming pools - sales, supplies and services");
        mccs.put("5997", "Electric razor shops - sales and service");
        mccs.put("5998", "Tent and awning shops");
        mccs.put("5999", "Miscellaneous and speciality retail outlets");
        mccs.put("7011", "Lodging - hotels, motels and resorts");
        mccs.put("7032", "Sporting and recreational camps");
        mccs.put("7033", "Trailer parks and camp-sites");
        mccs.put("7210", "Laundry, cleaning and garment services");
        mccs.put("7211", "Laundry services - family and commercial");
        mccs.put("7216", "Dry cleaners");
        mccs.put("7217", "Carpet and upholstery cleaning");
        mccs.put("7221", "Photographic studios");
        mccs.put("7230", "Beauty and barber shops");
        mccs.put("7251", "Shoe repair shops, shoe shine parlours and hat cleaning shops");
        mccs.put("7261", "Funeral services and crematoriums");
        mccs.put("7273", "Dating and escort services");
        mccs.put("7278", "Buying and shopping services and clubs");
        mccs.put("7295", "Babysitting and housekeeping services");
        mccs.put("7296", "Clothing rentals - costumes, uniforms and formal wear");
        mccs.put("7297", "Massage parlours");
        mccs.put("7298", "Health and beauty spas");
        mccs.put("7299", "Miscellaneous personal services - not elsewhere classified");
        mccs.put("7311", "Advertising services");
        mccs.put("7333", "Commercial photography, art and graphics");
        mccs.put("7338", "Quick copy, reproduction and blueprinting services");
        mccs.put("7339", "Stenographic and secretarial support services");
        mccs.put("7342", "Exterminating and disinfecting services");
        mccs.put("7349", "Cleaning, maintenance and janitorial services");
        mccs.put("7361", "Employment agencies and temporary help services");
        mccs.put("7372", "Computer programming, data processing and integrated systems design services");
        mccs.put("7375", "Information retrieval services");
        mccs.put("7379", "Computer maintenance and repair services - not elsewhere classified");
        mccs.put("7392", "Management, consulting and public relations services");
        mccs.put("7393", "Detective agencies, protective agencies and security services, including armoured cars and guard dogs");
        mccs.put("7394", "Equipment, tool, furniture and appliance rentals and leasing");
        mccs.put("7395", "Photofinishing laboratories and photo developing");
        mccs.put("7399", "Business services - not elsewhere classified");
        mccs.put("7511", "Reserved for national use");
        mccs.put("7512", "Automobile rentals");
        mccs.put("7513", "Truck and utility trailer rentals");
        mccs.put("7519", "Motor home and recreational vehicle rentals");
        mccs.put("7523", "Parking lots and garages");
        mccs.put("7531", "Automotive body repair shops");
        mccs.put("7534", "Tyre retreading and repair shops");
        mccs.put("7535", "Automotive paint shops");
        mccs.put("7538", "Automotive service shops (non-dealer)");
        mccs.put("7542", "Car washes");
        mccs.put("7549", "Towing services");
        mccs.put("7622", "Electronics repair shops");
        mccs.put("7623", "Air conditioning and refrigeration repair shops");
        mccs.put("7629", "Electrical and small appliance repair shops");
        mccs.put("7631", "Watch, clock and jewellery repair shops");
        mccs.put("7641", "Furniture reupholstery, repair and refinishing");
        mccs.put("7692", "Welding services");
        mccs.put("7699", "Miscellaneous repair shops and related services");
        mccs.put("7829", "Motion picture and video tape production and distribution");
        mccs.put("7832", "Motion picture theatres");
        mccs.put("7841", "Video tape rentals");
        mccs.put("7911", "Dance halls, studios and schools");
        mccs.put("7922", "Theatrical producers (except motion pictures) and ticket agencies");
        mccs.put("7929", "Bands, orchestras and miscellaneous entertainers - not elsewhere classified");
        mccs.put("7932", "Billiard and pool establishments");
        mccs.put("7933", "Bowling alleys");
        mccs.put("7941", "Commercial sports, professional sports clubs, athletic fields and sports promoters");
        mccs.put("7991", "Tourist attractions and exhibits");
        mccs.put("7992", "Public golf courses");
        mccs.put("7993", "Video amusement game supplies");
        mccs.put("7994", "Video game arcades and establishments");
        mccs.put("7996", "Amusement parks, circuses, carnivals and fortune tellers");
        mccs.put("7997", "Membership clubs (sports, recreation, athletic), country clubs and private golf courses");
        mccs.put("7998", "Aquariums, seaquariums and dolphinariums");
        mccs.put("7999", "Recreation services - not elsewhere classified");
        mccs.put("8011", "Doctors and physicians - not elsewhere classified");
        mccs.put("8021", "Dentists and orthodontists");
        mccs.put("8031", "Osteopaths");
        mccs.put("8041", "Chiropractors");
        mccs.put("8042", "Optometrists and ophthalmologists");
        mccs.put("8043", "Opticians, optical goods and eyeglasses");
        mccs.put("8049", "Podiatrists and chiropodists");
        mccs.put("8050", "Nursing and personal care facilities");
        mccs.put("8062", "Hospitals");
        mccs.put("8071", "Medical and dental laboratories");
        mccs.put("8099", "Medical services and health practitioners - not elsewhere classified");
        mccs.put("8111", "Legal services and attorneys");
        mccs.put("8211", "Elementary and secondary schools");
        mccs.put("8220", "Colleges, universities, professional schools and junior colleges");
        mccs.put("8241", "Correspondence schools");
        mccs.put("8244", "Business and secretarial schools");
        mccs.put("8249", "Trade and vocational schools");
        mccs.put("8299", "Schools and educational services - not elsewhere classified");
        mccs.put("8351", "Child care services");
        mccs.put("8641", "Civic, social and fraternal associations");
        mccs.put("8675", "Automobile associations");
        mccs.put("8699", "Membership organizations - not elsewhere classified");
        mccs.put("8734", "Testing laboratories (non-medical)");
        mccs.put("8911", "Architectural, engineering and surveying services");
        mccs.put("8931", "Accounting, auditing and bookkeeping services");
        mccs.put("8999", "Professional services - not elsewhere classified");
        mccs.put("9399", "Government Services - not elsewhere classified");
        return mccs;
    }

}


