package org.abelsromero.box.sdk.helpers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ABEL.SALGADOROMERO on 25/08/2016.
 */
public class MapBuilder {

    private Map<String, Object> values;

    private MapBuilder() {
        this.values = new HashMap();
    }

    public static MapBuilder emptyMap() {
        return new MapBuilder();
    }

    public MapBuilder put(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return values;
    }

}
