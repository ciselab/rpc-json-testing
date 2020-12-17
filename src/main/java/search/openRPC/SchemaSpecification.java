package search.openRPC;

import org.json.JSONArray;
import org.json.JSONObject;

public class SchemaSpecification {

    private String type;

    private Long min;
    private Long max;

    private String pattern;
    private String[] enums;

    public SchemaSpecification(JSONObject specification, JSONObject schema) {
        this.type = schema.getString("type");

        if (type.equals("integer")) {
            min = schema.has("minimum") ? schema.getLong("minimum") : 0L;
            max = schema.has("maximum") ? schema.getLong("maximum") : Long.MAX_VALUE;
        } else if (type.equals("string")) {
            pattern = schema.has("pattern") ? schema.getString("pattern") : null;

            if (schema.has("enum")) {
                JSONArray options = schema.getJSONArray("enum");
                enums = new String[options.length()];

                for (int i = 0; i < options.length(); i++) {
                    enums[i] = options.getString(i);
                }
            }
        } else if (type.equals("object")) {
            // TODO
        }

        // TODO more options



    }

    public String getType() {
        return type;
    }

    public Long getMin() {
        return min;
    }

    public Long getMax() {
        return max;
    }

    public String getPattern() {
        return pattern;
    }

    public String[] getEnums() {
        return enums;
    }
}
