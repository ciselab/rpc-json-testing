package search;

import com.github.curiousoddman.rgxgen.RgxGen;
import org.json.JSONObject;
import search.genes.ArrayGene;
import search.genes.BooleanGene;
import search.genes.Gene;
import search.genes.JSONObjectGene;
import search.genes.LongGene;
import search.genes.StringGene;
import search.openRPC.ParamSpecification;
import search.openRPC.SchemaSpecification;
import search.openRPC.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomIndex;

public class Generator {

    private Specification specification;

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
            // TODO this value is rather random and should be specified in some properties/config file
            if (param.isRequired() || getRandom().nextDouble() < 0.25) {
                List<SchemaSpecification> schemaOptions = specification.getSchemas().get(param.getPath());
                // if there is only one possible schema, this will be the type
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
        Long minimum = schema.getMin();
        Long maximum = schema.getMax();

        String type = schema.getType();
        String pattern = schema.getPattern();
        String[] enumOptions = schema.getEnums();

        switch (type) {
            case "object":
                JSONObjectGene objectGene = new JSONObjectGene(schema);

                Map<String, List<SchemaSpecification>> children = schema.getChildSchemaSpecification();
                Set<String> required = schema.getRequiredKeys();

                for (String key : children.keySet()) {
                    // if a key is not required there is 75 % chance to be skipped
                    if (!required.contains(key) && getRandom().nextDouble() >= 0.25) {
                        continue;
                    }

                    List<SchemaSpecification> options = children.get(key);
                    // take random schema out of the option
                    SchemaSpecification choice = options.get(getRandom().nextInt(options.size()));

                    objectGene.addChild(new StringGene(null, key), generateValueGene(choice));
                }

                return objectGene;
            case "array":
                List<SchemaSpecification> items = schema.getArrayItemSchemaSpecification();
                // TODO probably always just one type of child
                ArrayGene arrayGene = new ArrayGene(schema);

                for (int i = 0; i < getRandom().nextInt(ArrayGene.MAX_ARRAY_SIZE); i++) {
                    arrayGene.addChild(generateValueGene(items.get(0)));
                }

                return arrayGene;
            case "boolean":
                return new BooleanGene(schema, getRandom().nextBoolean());
            case "integer":
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
                    return new StringGene(schema, generateRandomValue("[a-z]*"));
                }
        }
    }

    /**
     * Generate a string based on a Regex expression.
     * @param regex
     * @return String
     */
    public String generateRandomValue(String regex) {
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
        if (getRandom().nextDouble() > 0.75) {
            method = methods[1];
        }
        return method;
    }

}
