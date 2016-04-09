package org.alexiwilius.sehirsenin.res;

/**
 * Created by AlexiWilius on 27.12.2014.
 */
public class Param {

    public static final String DATA_IDENTIFIER = "SEHIR_SENIN_IDENTIFIER";

    public static final String CARD_NUMBER = "CARD_NUMBER";

    public static final String SERVICE_URL = "https://servisler.izmir.bel.tr/Service1.svc";

    public static final String ARG_ZOOM_AMOUNT = "map_zoom_amount";

    public static final long MAP_STATION_UPDATE_INTERVAL = 5000;

    public interface SOAPActions {
        String BALANCE = "http://tempuri.org/IService1/UlasimKartiBakiyesiGetir";
        String INCOMING_BUS_LIST = "http://tempuri.org/IService1/YaklasanOtobusleriDuragaGoreGetir";
        String BUS_STOP_BUS_LIST = "http://tempuri.org/IService1/DurakAra";
        String LINE_DEPARTURE_LIST = "http://tempuri.org/IService1/HareketSaatleriniHattaTarifeyeGoreGetir";
        String LINE_PATH_LIST = "http://tempuri.org/IService1/HatDuraklariniGetir";
    }
}
