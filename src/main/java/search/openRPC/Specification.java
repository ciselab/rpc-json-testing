package search.openRPC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import search.Generator;
import search.genes.Gene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.RandomSingleton.getRandom;

/**
 * Specification.
 * Uses the OpenRPC specification (a JSONObject) to retrieve required methods and corresponding params.
 */
public class Specification {

    private String key;
    private String location;
    private JSONObject object;
    private JSONObject fullSpecification;
    private Map<String, Specification> children;
    private Generator generator;

    public Specification(String key, String location, JSONObject object, JSONObject fullSpecification, Generator generator) {
        this.key = key;
        this.location = location;
        this.object = object;
        this.fullSpecification = fullSpecification;
        this.generator = generator;
        this.children = new HashMap<>();

        // continue down the tree
        this.processChildren();
    }

    private void processChildren() {
        if (this.location.equals("root")) {
            JSONArray allMethods = object.getJSONArray("methods");

            // Loop through all the listed methods and add them to the list.
            for (int i = 0; i < allMethods.length(); i++) {
                children.put(allMethods.getJSONObject(i).getString("name"), new Specification(allMethods.getJSONObject(i).getString("name"), "method", allMethods.getJSONObject(i), this.fullSpecification, this.generator));
            }
        } else if (this.location.equals("method")) {
            JSONArray params = object.getJSONArray("params");

            for (int i = 0; i < params.length(); i++) {
                JSONObject param = params.getJSONObject(i);
                if (param.has("$ref")) {
                    param = resolveRef(param.getString("$ref"));
                }
                children.put(param.getString("name"), new Specification(param.getString("name"), "param", param, this.fullSpecification, this.generator));

            }
        } else if (this.location.equals("param")) {
            JSONObject schema = object.getJSONObject("schema");

            if (schema.has("anyOf")) {
                for (int i = 0; i < schema.getJSONArray("anyOf").length(); i++) {
                    children.put("" + i, new Specification("" + i, "type", schema.getJSONArray("anyOf").getJSONObject(i), this.fullSpecification, this.generator));
                }
            } else if (schema.has("type") && schema.get("type") instanceof JSONArray) {
                for (int i = 0; i < schema.getJSONArray("type").length(); i++) {
                    JSONObject temp = new JSONObject(schema);
                    temp.put("type", schema.getJSONArray("type").getString(i));
                    children.put("" + i, new Specification("" + i, "type", temp, this.fullSpecification, this.generator));
                }
            } else {
                if (schema.has("$ref")) {
                    schema = resolveRef(schema.getString("$ref"));
                }

                children.put("0", new Specification("0", "type", schema, this.fullSpecification, this.generator));
            }
        }
    }

    /**
     * Find the object that is referenced to in the JSON file.
     *
     * @param ref
     * @return JSONObject
     */
    public JSONObject resolveRef(String ref) {
        ref = ref.substring(ref.indexOf("#") + 2);
        String[] pathPieces = ref.split("/");

        JSONObject object = this.fullSpecification;
        for (String pathPiece : pathPieces) {
            if (object.has(pathPiece)) {
                object = object.getJSONObject(pathPiece);
            } else {
                throw new JSONException("Could not find ref");
            }
        }
        return object;
    }

    /**
     * Get a different option (e.g. from one method to another, or from one type to another)
     * @return location
     */
    public Gene getRandomOption() {
        List<String> keys = new ArrayList<>(children.keySet());

        int index = getRandom().nextInt(keys.size());

        Specification specification = children.get(keys.get(index));

        // call generator using specification for type
        return generator.generateFromSpecification(key, keys.get(index), specification);
    }

    public Specification getChild(Gene child) {
        // Do not continue further than param (tree ends here)
        if (location.equals("param")) {
            return this;
        }

        if (!children.containsKey(child.getKey())) {
            throw new IllegalStateException("Invalid key: " + child.getKey());
        }

        return children.get(child.getKey());
    }

    public Map<String, Specification> getChildren() {
        return children;
    }

    public String getLocation() {
        return location;
    }

    public JSONObject getObject() {
        return object;
    }

    public Generator getGenerator() {
        return generator;
    }

    @Override
    public String toString() {
        return "Specification{" +
            "location='" + location + '\'' +
//            ", object=" + object +
//            ", fullSpecification=" + fullSpecification +
            ", children=" + children +
//            ", generator=" + generator +
            '}';
    }
}
