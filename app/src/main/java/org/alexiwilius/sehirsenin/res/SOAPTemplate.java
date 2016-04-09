package org.alexiwilius.sehirsenin.res;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by AlexiWilius on 19.10.2015.
 */
public class SOAPTemplate {

    public static Document balance(String cardId) throws ParserConfigurationException, IOException, SAXException {
        return createDocument("" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">" +
                header +
                "   <soapenv:Body>" +
                "      <tem:UlasimKartiBakiyesiGetir>" +
                "         <tem:kartNumarasi>" + cardId + "</tem:kartNumarasi>" +
                "      </tem:UlasimKartiBakiyesiGetir>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>");
    }

    public static Document station(String stationId) throws ParserConfigurationException, IOException, SAXException {
        return createDocument("" +
                "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:tem='http://tempuri.org/'>" +
                header +
                "    <soapenv:Body>" +
                "        <tem:YaklasanOtobusleriDuragaGoreGetir>" +
                "            <tem:durakId>" + stationId + "</tem:durakId>" +
                "        </tem:YaklasanOtobusleriDuragaGoreGetir>" +
                "    </soapenv:Body>" +
                "</soapenv:Envelope>");
    }

    public static Document busList(String stationId) throws IOException, SAXException, ParserConfigurationException {
        return createDocument("" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">" +
                header +
                "   <soapenv:Body>" +
                "       <tem:DurakAra>" +
                "           <tem:anahtarKelime>" + stationId + "</tem:anahtarKelime>" +
                "       </tem:DurakAra>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>");
    }

    public static Document departureTimes(String line, String day) throws IOException, SAXException, ParserConfigurationException {
        return createDocument("" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">" +
                header +
                "   <soapenv:Body>" +
                "      <tem:HareketSaatleriniHattaTarifeyeGoreGetir>" +
                "         <tem:eshotHatId>" + line + "</tem:eshotHatId>" +
                "         <tem:tarifeId>" + day + "</tem:tarifeId>" +
                "      </tem:HareketSaatleriniHattaTarifeyeGoreGetir>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>");
    }

    public static Document getLinePath(String line, Integer direction) throws IOException, SAXException, ParserConfigurationException {
        return createDocument("" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">" +
                header +
                "" +
                "   <soapenv:Body>" +
                "      <tem:HatDuraklariniGetir>" +
                "         <tem:eshotHatId>" + line + "</tem:eshotHatId>" +
                "         <tem:yon>" + direction + "</tem:yon>" +
                "      </tem:HatDuraklariniGetir>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>");
    }

    private static Document createDocument(String s) throws ParserConfigurationException, IOException, SAXException {
        return DocumentBuilderFactory
                .newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(s.getBytes()));
    }

    private static String header = "" +
            "    <soapenv:Header>" +
            "        <n011:UserName xmlns:n011='CH1'>" +
            "            UI5YorOSKe2os3UFm3hGr+cO8jPNos+PCBqvXK9nwMGimQ3TYgK1bC6gKimagbw1aruV0gKYMFWLVvyyXPBvuJsdmAVMdpKDxJpl7O7PdmzK6CgrbSgVYsql12JAYob8oF7/1X6NA8c/Gh/63aVuto76A0u4VwPSlBuCa0m+Wvs=" +
            "        </n011:UserName>" +
            "        <n1:Password xmlns:n1='CH2'>" +
            "            yPAdUVkDMcwSrezSRYX62+Hd9rIbiXHCJvn6/rZ/aI358IsmUq+DGJT4gxAl1xs7eX/HMeX+A8yReAlN90Qo//fkiKdYbYhze3+fl2lR1Zl3EgiRBfIo+Nwf1+3JlmJot6TUXRnN5b7441lpFlRS1rtN5WFsyf0V32dQagkuckw=" +
            "        </n1:Password>" +
            "    </soapenv:Header>";
}
