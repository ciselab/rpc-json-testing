package openRPC;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static openRPC.Specification.extractTypes;

/**
 * SchemaSpecification contains the attributes of a schema belonging to a parameter.
 */
public class SchemaSpecification {

    private String name;
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

    private boolean isMutable; // indicates whether a parameter is allowed to be mutated.

    /**
     * Reads the OpenRPC specification for a schema.
     * @param schema
     */
    public SchemaSpecification(JSONObject schema) {
        // Immutable schemas are given a name that is checked to see if it is immutable (if so, it is a ConstantStringGene).
        // This is necessary to use various correct parameter values etc. corresponding to the SUT.
        this.name = "";
        this.isMutable = true;

        if (schema.has("type")) {
            this.type = schema.getString("type");
        } else if (schema.has("items")) {
            this.type = "array";
        } else {
            throw new RuntimeException("Schema has no type: " + schema.toString());
        }

        if (type.equals("integer")) {
            min = schema.has("minimum") ? schema.getLong("minimum") : 0L;
            max = schema.has("maximum") ? schema.getLong("maximum") : Long.MAX_VALUE;
        } else if (type.equals("string")) {
            pattern = schema.has("pattern") ? schema.getString("pattern") : null;

            if (pattern != null && (
                    pattern.contains("__ACCOUNT__")
                    || pattern.contains("__MASTER_KEY__")
                    || pattern.contains("__MASTER_SEED__")
                    || pattern.contains("__MASTER_SEED_HEX__")
                    || pattern.contains("__PUBLIC_KEY__")
                    || pattern.contains("__PUBLIC_KEY_HEX__")
                )) {
                this.isMutable = false;
                this.name = pattern.replace("$", "").replace("^", "");
            }

            if (schema.has("enum")) {
                JSONArray options = schema.getJSONArray("enum");

                enums = new String[options.length()];

                for (int i = 0; i < options.length(); i++) {
                    enums[i] = options.getString(i);
                    if (options.getString(i).equals("__ACCOUNT__")
                        || options.getString(i).equals("__MASTER_KEY__")
                        || options.getString(i).equals("__MASTER_SEED__")
                        || options.getString(i).equals("__MASTER_SEED_HEX__")
                        || options.getString(i).equals("__PUBLIC_KEY__")
                        || options.getString(i).equals("__PUBLIC_KEY_HEX__")) {
                        this.isMutable = false;
                        this.name = options.getString(i);
                    }
                }
            }
        } else if (type.equals("object")) {
            requiredKeys = new HashSet<>();
            childSchemaSpecification = new HashMap<>() ;
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
            if (schema.has("items")) {
                arrayItemSchemaSpecification = extractTypes(schema.getJSONObject("items"));
            }
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

    public boolean isMutable() {
        return isMutable;
    }

    public String getName() {
        return name;
    }
}
