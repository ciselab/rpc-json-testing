package search;

import com.github.curiousoddman.rgxgen.RgxGen;
import search.genes.ArrayGene;
import search.genes.BooleanGene;
import search.genes.Gene;
import search.genes.JSONObjectGene;
import search.genes.LongGene;
import search.genes.StringGene;
import openRPC.ParamSpecification;
import openRPC.SchemaSpecification;
import openRPC.Specification;
import util.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomBool;
import static util.RandomSingleton.getRandomIndex;

public class Generator {

    private Specification specification;
    private String regexDefault = "[a-z]*";

    public Generator(Specification specification) {
        this.specification = specification;
    }

    public SchemaSpecification getSchema(String path, String type) {
        List<SchemaSpecification> schemas = specification.getSchemas().get(path);

        for (SchemaSpecification schema : schemas) {
            if (schema.getType().equals(type)) {
                return schema;
            }
        }

        throw new IllegalArgumentException("Cannot find schema of type: " + type);
    }

    /**
     * Remove a method from the specification so that it cannot be used again.
     * @param method
     */
    public void removeMethod(String method) {
        this.specification.getMethods().remove(method);
    }

    public String getRandomMethod() {
        List<String> methods = new ArrayList<>(specification.getMethods().keySet());
        return methods.get(getRandomIndex(methods));
    }

    /**
     * Retrieve the arrayGene from the given specification that corresponds to a method.
     * @param name
     * @return ArrayGene
     */
    public ArrayGene generateMethod(String name) {
        List<ParamSpecification> params = specification.getMethods().get(name);

        ArrayGene arrayGene = new ArrayGene(null);
        JSONObjectGene objectGene = new JSONObjectGene(null);
        arrayGene.addChild(objectGene);

        for (ParamSpecification param : params) {
            if (param.isRequired() || getRandom().nextDouble() < Configuration.INCLUDE_PARAM_PROB) {
                List<SchemaSpecification> schemaOptions = specification.getSchemas().get(param.getPath());
                // If there is only one possible schema, this will be the type.
                int index = getRandom().nextInt(schemaOptions.size());

                SchemaSpecification specification = schemaOptions.get(index);

                objectGene.addChild(new StringGene(null, param.getName()), generateValueGene(specification));
            }
        }

        return arrayGene;
    }

    private List<SchemaSpecification> getSchemaSpecification(String path) {
        return specification.getSchemas().get(path);
    }

    /**
     * Retrieve the Gene from the given specification that corresponds to the type(s) of a param.
     * @param schema
     * @return Gene
     */
    public Gene generateValueGene(SchemaSpecification schema) {
        String type = schema.getType();

        // With some probably alter the type of the gene
        if (getRandomBool(Configuration.CHANGE_TYPE_PROB)) {
            if (Configuration.ADVANCED_TYPE_CHANGES) {
                int randomSchema = getRandomIndex(specification.getSchemas().values());
                List<SchemaSpecification> options = (new ArrayList<>(specification.getSchemas().values())).get(randomSchema);
                schema = options.get(getRandomIndex(options));
                type = schema.getType();
            } else {
                int typeToBe = getRandom().nextInt(3);
                if (typeToBe == 0) {
                    type = "string";
                } else if (typeToBe == 1) {
                    type = "boolean";
                } else if (typeToBe == 2) {
                    type = "integer";
                }
            }
        }

        Long minimum = schema.getMin();
        Long maximum = schema.getMax();

        String pattern = schema.getPattern();
        String[] enumOptions = schema.getEnums();

        switch (type) {

            case "object":
                JSONObjectGene objectGene = new JSONObjectGene(schema);

                Map<String, List<SchemaSpecification>> children = schema.getChildSchemaSpecification();
                Set<String> required = schema.getRequiredKeys();

                for (String key : children.keySet()) {
                    // If a key is not required there is probability that it is not included
                    if (!required.contains(key) && getRandom().nextDouble() <= Configuration.SKIP_NONREQUIRED_KEY_PROB) {
                        continue;
                    }

                    List<SchemaSpecification> options = children.get(key);
                    // Pick a random schema out of the options
                    SchemaSpecification choice = options.get(getRandom().nextInt(options.size()));

                    objectGene.addChild(new StringGene(null, key), generateValueGene(choice));
                }

                return objectGene;

            case "array":
                List<SchemaSpecification> items = schema.getArrayItemSchemaSpecification();
                // TODO probably always just one type of child
                ArrayGene arrayGene = new ArrayGene(schema);

                for (int i = 0; i < getRandom().nextInt(SchemaSpecification.MAX_ARRAY_SIZE); i++) {
                    arrayGene.addChild(generateValueGene(items.get(0)));
                }

                return arrayGene;

            case "boolean":
                return new BooleanGene(schema, getRandom().nextBoolean());

            case "integer":
                if (minimum == null) {
                    minimum = Long.MIN_VALUE;
                }
                if (maximum == null) {
                    maximum = Long.MAX_VALUE;
                }
                long generatedLong = minimum + (long) (getRandom().nextDouble() * (maximum - minimum));
                return new LongGene(schema, generatedLong);

            case "string":
            default:
                if (enumOptions != null && enumOptions.length != 0) {
                    // pick a random enum type
                    return new StringGene(schema, enumOptions[getRandom().nextInt(enumOptions.length)]);
                } else if (pattern != null) {
                    // create a value from the pattern
                    return new StringGene(schema, generateRandomValue(pattern));
                } else {
                    // create a string from scratch
                    return new StringGene(schema, generateRandomValue(regexDefault));
                }
        }
    }

    /**
     * Generate a string based on a Regex expression.
     * @param regex
     * @return String
     */
    public static String generateRandomValue(String regex) {
        //TODO: investigate rgxGen lib and alternatives (there might be better ones)

        RgxGen rgxGen = new RgxGen(regex);

        return rgxGen.generate(getRandom());
    }

    /**
     * Generate the HTTPMethod for an Individual.
     * @return String the HTTP method
     */
    public String generateHTTPMethod() {
        String[] methods = new String[]{"POST", "GET"};
        String method = methods[0];
        if (getRandom().nextDouble() > Configuration.HTTP_METHOD_GET_PROB) {
            method = methods[1];
        }
        return method;
    }

}
