package org.abelsromero.box.sdk;


import org.json.JSONObject;

import java.util.Map;

/**
 * Created by ABEL.SALGADOROMERO on 29/07/2016.
 */
public class MetadataTemplate {

    // aka. tamplate name
    private String type;
    private JSONObject values;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getValues() {
        return values;
    }

    public void setValues(JSONObject values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "{" + type + ":" + values.toString() + "}";
    }

}
