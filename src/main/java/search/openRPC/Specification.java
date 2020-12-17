package search.openRPC;

import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import search.Generator;
import search.genes.Gene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Specification.
 * Uses the OpenRPC specification (a JSONObject) to retrieve required methods and corresponding params.
 */
public class Specification {

    private static String separator = "/";

    private Map<String, List<ParamSpecification>> methods;
    private Map<String, List<SchemaSpecification>> schemas;
    private JSONObject object;

    public Specification(JSONObject object) {
        this.object = object;

        this.methods = new HashMap<>();
        this.schemas = new HashMap<>();

        this.processChildren();
    }

    private void processChildren() {

        Queue<Pair<String, JSONObject>> pq = new LinkedList<>();

        pq.add(new Pair<>("#", this.object));

        while (!pq.isEmpty()) {
            Pair<String, JSONObject> pair = pq.poll();
            String path = pair.getKey();
            JSONObject object = pair.getValue();

            for (Iterator it = object.keys(); it.hasNext(); ) {
                String key = (String) it.next();

                if (key.equals("$ref")) {
                    JSONObject ref = resolveRef(object.getString("$ref"));
                    pq.add(new Pair<>(path, ref));
                } else if (key.equals("schema")) {
                    // value
                    this.schemas.put(path + separator + key, extractTypes(object.getJSONObject(key)));
                } else if (object.get(key) instanceof JSONObject) {
                    pq.add(new Pair<>(path + separator + key, object.getJSONObject(key)));
                } else if (object.get(key) instanceof JSONArray) {
                    JSONArray array = object.getJSONArray(key);
                    String newPath = path + separator + key + separator;

                    if (key.equals("methods")) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject method = array.getJSONObject(i);
                            List<ParamSpecification> paramSpecifications = getParamInfo(newPath + i, method);

                            methods.put(method.getString("name"), paramSpecifications);
                        }
                    }

                    // TODO assumes that all arrays contain objects
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject next = array.getJSONObject(i);

                        pq.add(new Pair<>(newPath + i, next));
                    }

                    // TODO
                } else {
//                    // does not recognize keys, should continue
                }

            }

        }

    }

    private List<ParamSpecification> getParamInfo (String path, JSONObject object) {
        List<ParamSpecification> paramSpecifications = new ArrayList<>();

        JSONArray params = object.getJSONArray("params");

        path += separator + "params";

        for (int i = 0; i < params.length(); i++) {
            JSONObject param = params.getJSONObject(i);

            if (param.has("$ref")) {
                param = resolveRef(param.getString("$ref"));
            }

            String name = param.getString("name");
            boolean required = param.has("required") && param.getBoolean("required");

            paramSpecifications.add(new ParamSpecification(name, path + separator + i + separator + "schema", required));
        }

        return paramSpecifications;
    }

    private List<SchemaSpecification> extractTypes(JSONObject schema) {
        List<SchemaSpecification> types = new ArrayList<>();

        if (schema.has("anyOf")) {
            for (int i = 0; i < schema.getJSONArray("anyOf").length(); i++) {
                types.add(new SchemaSpecification(this.object, schema.getJSONArray("anyOf").getJSONObject(i)));
            }
        } else if (schema.has("type") && schema.get("type") instanceof JSONArray) {
            for (int i = 0; i < schema.getJSONArray("type").length(); i++) {
                JSONObject temp = new JSONObject(schema);
                temp.put("type", schema.getJSONArray("type").getString(i));
                types.add(new SchemaSpecification(this.object, temp));
            }
        } else {
            if (schema.has("$ref")) {
                schema = resolveRef(schema.getString("$ref"));
            }

            types.add(new SchemaSpecification(this.object, schema));
        }

        return types;
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

        JSONObject object = this.object;
        for (String pathPiece : pathPieces) {
            if (object.has(pathPiece)) {
                object = object.getJSONObject(pathPiece);
            } else {
                throw new JSONException("Could not find ref");
            }
        }
        return object;
    }


    public JSONObject getObject() {
        return object;
    }

    public Map<String, List<ParamSpecification>> getMethods() {
        return methods;
    }

    public Map<String, List<SchemaSpecification>> getSchemas() {
        return schemas;
    }
}

