package edu.uchicago.cs.db.subattr.compare.dataload;

import java.net.URI;
import java.util.Set;

public class ColumnData {

    int id;

    String dataType;

    URI fileUri;

    ColumnData parent;

    Set<Feature> features;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public URI getFileUri() {
        return fileUri;
    }

    public void setFileUri(URI fileUri) {
        this.fileUri = fileUri;
    }

    public ColumnData getParent() {
        return parent;
    }

    public void setParent(ColumnData parent) {
        this.parent = parent;
    }

    public Set<Feature> getFeatures() {
        return features;
    }
}
