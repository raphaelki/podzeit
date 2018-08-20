package de.rkirchner.podzeit.data.remote;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.rkirchner.podzeit.data.models.Series;
import okhttp3.ResponseBody;
import retrofit2.Converter;

public class RSSResponseBodyConverter<T> implements Converter<ResponseBody, Series> {

    @Override
    public Series convert(ResponseBody value) throws IOException {
        Series series = new Series();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            RSSParser parser = new RSSParser();
            saxParser.parse(value.byteStream(), parser);
            series = parser.getSeries();
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        } finally {
            value.close();
        }
        return series;
    }
}
