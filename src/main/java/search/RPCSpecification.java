//package search;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import util.IO;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class RPCSpecification {
//
//    private JSONObject specification;
//
//    private List<Method> methods;
//
//    /**
//     * Create the RPCSpecification based on the OpenRPC file.
//     * @param filepath
//     */
//    public RPCSpecification(String filepath) {
//        try {
//            String data = IO.readFile(filepath);
//            this.specification = new JSONObject(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Retrieve all methods and corresponding parameters for this JSON-RPC API.
//     */
//    public void process() {
//        JSONArray allMethods = specification.getJSONArray("methods");
//
//        // Loop through all the listed methods and add them to the list.
//        this.methods = new ArrayList<>();
//        for (int i = 0; i < allMethods.length(); i++) {
//            methods.add(new Method(allMethods.getJSONObject(i), this));
//        }
//    }
//
//    public List<Method> getMethods() {
//        return this.methods;
//    }
//
//    /**
//     * Find the object that is referenced to in the JSON file.
//     * @param ref
//     * @return JSONObject
//     */
//    public JSONObject resolveRef(String ref) {
//        ref = ref.substring(ref.indexOf("#") + 2);
//        String[] pathPieces = ref.split("/");
//
//        JSONObject object = this.specification;
//        for (String pathPiece : pathPieces) {
//            if (object.has(pathPiece)) {
//                object = object.getJSONObject(pathPiece);
//            } else {
//                throw new JSONException("Could not find ref");
//            }
//        }
//        return object;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder methodString = new StringBuilder();
//        for (Method m: methods) {
//            methodString.append(m.toString()).append("\n");
//        }
//        return "RPCSpecification{" +
//            "specification=" + specification +
//            ", methods=\n" + methodString.toString() +
//            '}';
//    }
//}
//
//class Method {
//
//    private String name;
//    private List<Parameter> parameters;
//
//    /**
//     * Constructor for the Method object. Creates a Method based on the OpenRPC specification.
//     * The OpenRPC format requires each method to have a name (String), params (JSONArray) and result (JSONObject).
//     * @param methodSpecification
//     * @param specification
//     */
//    Method(JSONObject methodSpecification, RPCSpecification specification) {
//        this.name = methodSpecification.getString("name");
//
//        JSONArray params = methodSpecification.getJSONArray("params");
//
//        this.parameters = new ArrayList<>();
//        for (int i = 0; i < params.length(); i++) {
//            JSONObject param = params.getJSONObject(i);
//            if (param.has("$ref")) {
//                this.parameters.add(new Parameter(specification.resolveRef(param.getString("$ref")), specification));
//            } else {
//                this.parameters.add(new Parameter(param, specification));
//            }
//        }
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder parameterString = new StringBuilder();
//        for (Parameter p: parameters) {
//            parameterString.append(p.toString()).append("\n");
//        }
//
//        return "Method{" +
//            "name='" + name + '\'' +
//            ", parameters=\n" + parameterString.toString() +
//            '}';
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public List<Parameter> getParameters() {
//        return parameters;
//    }
//}
//
//class Parameter {
//
//    private String name;
//    private List<Type> types;
//    private boolean required;
//
//    /**
//     * Constructor for the Parameter object. Creates a Parameter based on the OpenRPC specification.
//     * The OpenRPC format requires each method to have a name (String) and a schema (JSONObject).
//     * @param parameterSpecification
//     * @param specification
//     */
//    Parameter(JSONObject parameterSpecification, RPCSpecification specification) {
//        this.name = parameterSpecification.getString("name");
//        this.required = parameterSpecification.has("required") && parameterSpecification.getBoolean("required");
//        this.types = new ArrayList<>();
//
//        JSONObject schema = parameterSpecification.getJSONObject("schema");
//
//        if (schema.has("anyOf")) {
//            for (int i = 0; i < schema.getJSONArray("anyOf").length(); i++) {
//                types.add(new Type(schema.getJSONArray("anyOf").getJSONObject(i)));
//            }
//        } else if (schema.has("type") && schema.get("type") instanceof JSONArray) {
//            for (int i = 0; i < schema.getJSONArray("type").length(); i++) {
//                JSONObject temp = new JSONObject(schema);
//                temp.put("type", schema.getJSONArray("type").getString(i));
//                types.add(new Type(temp));
//            }
//        } else {
//            if (schema.has("$ref")) {
//                schema = specification.resolveRef(schema.getString("$ref"));
//            }
//
//            types.add(new Type(schema));
//        }
//    }
//
//    public void sampleGene() {
//        // TODO
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder typesString = new StringBuilder();
//        for (Type t : types) {
//            typesString.append(t.toString()).append("\n");
//        }
//
//        return "Parameter{" +
//            "name='" + name + '\'' +
//            ", types=\n" + typesString.toString() +
//            ", required=" + required +
//            '}';
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public List<Type> getTypes() {
//        return types;
//    }
//
//    public boolean isRequired() {
//        return required;
//    }
//}
//
//class Type {
//
//    private String type;
//
//    private Long minimum;
//    private Long maximum;
//
//    private String pattern;
//    private List<String> enumOptions;
//
//
//    /**
//     * Constructor for the Type object. Creates a Type based on the OpenRPC specification.
//     * Currently only deals with optional terms 'type', 'pattern', 'enum'.
//     * @param typeSpecification
//     */
//    Type(JSONObject typeSpecification) {
//        this.minimum = typeSpecification.has("minimum") ? typeSpecification.getLong("minimum") : null;
//        this.maximum = typeSpecification.has("maximum") ? typeSpecification.getLong("maximum") : null;
//
//        this.type = typeSpecification.has("type") ? typeSpecification.getString("type") : null;
//        this.pattern = typeSpecification.has("pattern") ? typeSpecification.getString("pattern") : null;
//        this.enumOptions = new ArrayList<>();
//
//        if (typeSpecification.has("enum")) {
//            JSONArray array = typeSpecification.getJSONArray("enum");
//            for (int i = 0; i < array.length(); i++) {
//                this.enumOptions.add(array.getString(i));
//            }
//        }
//    }
//
//    @Override
//    public String toString() {
//        return "Type{" +
//            "type='" + type + '\'' +
//            ", minimum=" + minimum +
//            ", maximum=" + maximum +
//            ", pattern='" + pattern + '\'' +
//            ", enumOptions=" + enumOptions +
//            '}';
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public Long getMinimum() {
//        return minimum;
//    }
//
//    public Long getMaximum() {
//        return maximum;
//    }
//
//    public String getPattern() {
//        return pattern;
//    }
//
//    public List<String> getEnumOptions() {
//        return enumOptions;
//    }
//}