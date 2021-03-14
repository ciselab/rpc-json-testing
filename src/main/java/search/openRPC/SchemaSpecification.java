package search.openRPC;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static search.openRPC.Specification.extractTypes;

public class SchemaSpecification {

    private String type;

    // for LongGenes
    private Long min;
    private Long max;

    // for StringGenes
    private String pattern;
    private String[] enums;

    // for ArrayGenes
    private Long length;
    public static final Integer MAX_ARRAY_SIZE = 10;

    // for JSONObjectGenes
    private Set<String> requiredKeys;
    private Map<String, List<SchemaSpecification>> childSchemaSpecification;

    private List<SchemaSpecification> arrayItemSchemaSpecification;

    public SchemaSpecification(JSONObject schema) {
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
            requiredKeys = new HashSet<>();
            childSchemaSpecification = new HashMap<>();
            if (schema.has("required")) {
                JSONArray requiredArgs = schema.getJSONArray("required");

                for (int i = 0; i < requiredArgs.length(); i++) {
                    requiredKeys.add(requiredArgs.getString(i));
                }
            }
            if (schema.has("properties")) {
                JSONObject args = schema.getJSONObject("properties");

                for (Iterator it = args.keys(); it.hasNext(); ) {
                    String key = (String) it.next();
                    childSchemaSpecification.put(key, extractTypes(args.getJSONObject(key)));
                }
            }
        } else if (type.equals("array")) {
            length = schema.has("length") ? schema.getLong("length") : MAX_ARRAY_SIZE;
            System.out.println(length);
            if (schema.has("items")) {
                arrayItemSchemaSpecification = extractTypes(schema.getJSONObject("items"));
            }
//            if (schema.has("length")) {
//                length = schema.getLong("length");
//            }
        }
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

    public Long getLength() { return length; }

    public Set<String> getRequiredKeys() {
        return requiredKeys;
    }

    public Map<String, List<SchemaSpecification>> getChildSchemaSpecification() {
        return new HashMap<>(childSchemaSpecification);
    }

    public List<SchemaSpecification> getArrayItemSchemaSpecification() {
        return new ArrayList<>(arrayItemSchemaSpecification);
    }
}
