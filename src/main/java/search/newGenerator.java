//package search;
//
//import com.github.curiousoddman.rgxgen.RgxGen;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import search.genes.ArrayGene;
//import search.genes.BooleanGene;
//import search.genes.Gene;
//import search.genes.JSONObjectGene;
//import search.genes.LongGene;
//import search.genes.StringGene;
//import search.openRPC.Specification;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static util.RandomSingleton.getRandom;
//
//public class newGenerator {
//
//    /**
//     * Finds the gene(s) corresponding to a location in the given (part of a) specification.
//     * @param key
//     * @param specification
//     * @return String location
//     */
//    public Gene generateFromSpecification(String key, String childKey, Specification specification) {
//        if (specification.getLocation().equals("type")) {
//            return generateFromTypeSpecification(key, specification);
//        } else if (specification.getLocation().equals("param")) {
//            throw new IllegalStateException("Should not happen");
//        } else if (specification.getLocation().equals("method")) {
//            return generateFromMethodSpecification(key, childKey, specification);
//        } else if (specification.getLocation().equals("root")) {
//
//        }
//        throw new IllegalStateException("Undefined specification type");
//    }
//
//    /**
//     * Retrieve the arrayGene from the given specification that corresponds to a method.
//     * @param key
//     * @param specification
//     * @return ArrayGene
//     */
//    public Gene generateFromMethodSpecification(String key, String childKey, Specification specification) {
//        ArrayGene arrayGene = new ArrayGene(key);
//        JSONObjectGene objectGene = new JSONObjectGene(childKey);
//        arrayGene.addChild(objectGene);
//
//        for (String param : specification.getChildren().keySet()) {
//            if ((specification.getChildren().get(param).getObject().has("required") &&
//                specification.getChildren().get(param).getObject().getBoolean("required")) ||
//                getRandom().nextDouble() < 0.5) {
//                objectGene.addChild(new StringGene("", param), specification.getChildren().get(param).getRandomOption());
//            }
//        }
//
//        return arrayGene;
//    }
//
//    /**
//     * Retrieve the Gene from the given specification that corresponds to the type(s) of a param.
//     * @param key
//     * @param specification
//     * @return Gene
//     */
//    public Gene generateFromTypeSpecification(String key, Specification specification) {
//        JSONObject typeSpecification = specification.getObject();
//
//        Long minimum = typeSpecification.has("minimum") ? typeSpecification.getLong("minimum") : 0L;
//        Long maximum = typeSpecification.has("maximum") ? typeSpecification.getLong("maximum") : Long.MAX_VALUE;
//
//        String type = typeSpecification.has("type") ? typeSpecification.getString("type") : null;
//        String pattern = typeSpecification.has("pattern") ? typeSpecification.getString("pattern") : null;
//        List<String> enumOptions = new ArrayList<>();
//
//        if (typeSpecification.has("enum")) {
//            JSONArray array = typeSpecification.getJSONArray("enum");
//            for (int i = 0; i < array.length(); i++) {
//                enumOptions.add(array.getString(i));
//            }
//        }
//
//        switch (type) {
//            case "object":
//                //TODO: maybe change later (now gives error message response from API)
//                return new JSONObjectGene(key);
//            case "boolean":
//                return new BooleanGene(key, getRandom().nextBoolean());
//            case "integer":
//                long generatedLong = minimum + (long) (getRandom().nextDouble() * (maximum - minimum));
//
//                return new LongGene(key, generatedLong);
//            case "string":
//            default:
//                if (!enumOptions.isEmpty()) {
//                    // pick a random enum type
//                    return new StringGene(key, enumOptions.get(getRandom().nextInt(enumOptions.size())));
//                } else if (pattern != null) {
//                    // create a value from the pattern
//                    return new StringGene(key, generateRandomValue(pattern));
//                }
//        }
//
//        throw new IllegalArgumentException("Invalid parameter ");
//    }
//
//    /**
//     * Generate a string based on a Regex expression.
//     * @param regex
//     * @return String
//     */
//    public String generateRandomValue(String regex) {
//        //TODO: investigate rgxGen lib and alternatives (there might be better ones)
//
//        RgxGen rgxGen = new RgxGen(regex);
//
//        return rgxGen.generate(getRandom());
//    }
//
//    /**
//     * Generate the HTTPMethod for an Individual.
//     * @return String the HTTP method
//     */
//    public String generateHTTPMethod() {
//        String[] methods = new String[]{"POST", "GET"};
//        String method = methods[0];
//        if (getRandom().nextDouble() > 0.75) {
//            method = methods[1];
//        }
//        return method;
//    }
//
//}
