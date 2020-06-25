package edu.uchicago.cs.db.subattr.compare.dataload;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;

import javax.persistence.AttributeConverter;
import java.net.URI;
import java.net.URISyntaxException;

public class URIConverter implements AttributeConverter<URI, String>, org.eclipse.persistence.mappings.converters.Converter {

    public String convertToDatabaseColumn(URI objectValue) {
        if (objectValue == null)
            return null;
        return objectValue.toString();
    }

    public URI convertToEntityAttribute(String dataValue) {
        if (dataValue == null) {
            return null;
        }
        try {
            return new URI(dataValue);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convertObjectValueToDataValue(Object o, Session session) {
        return convertToDatabaseColumn((URI) o);
    }

    @Override
    public Object convertDataValueToObjectValue(Object o, Session session) {
        return convertToEntityAttribute((String) o);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void initialize(DatabaseMapping databaseMapping, Session session) {

    }
}
