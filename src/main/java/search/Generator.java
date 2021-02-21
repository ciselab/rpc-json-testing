package search;

import com.github.curiousoddman.rgxgen.RgxGen;
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

        ArrayGene arrayGene = new ArrayGene();
        JSONObjectGene objectGene = new JSONObjectGene();
        arrayGene.addChild(objectGene);

        for (ParamSpecification param : params) {
            // TODO this value is rather random and should be specified in some properties/config file
            if (param.isRequired() || getRandom().nextDouble() < 0.25) {
                objectGene.addChild(new StringGene("", param.getName()), generateValueGene(param.getPath()));
            }
        }

        return arrayGene;
    }

    /**
     * Retrieve the Gene from the given specification that corresponds to the type(s) of a param.
     * @param specPath
     * @return Gene
     */
    public Gene generateValueGene(String specPath) {
        List<SchemaSpecification> schemaOptions = specification.getSchemas().get(specPath);

        // if there is only one possible schema, this will be the type
        int index = getRandom().nextInt(schemaOptions.size());

        SchemaSpecification specification = schemaOptions.get(index);

        Long minimum = specification.getMin();
        Long maximum = specification.getMax();

        String type = specification.getType();
        String pattern = specification.getPattern();
        String[] enumOptions = specification.getEnums();

        switch (type) {
            case "object":
                //TODO: maybe change later (now gives error message response from API)
                return new JSONObjectGene();
            case "array":
                // TODO
                return new ArrayGene();
            case "boolean":
                return new BooleanGene(specPath, getRandom().nextBoolean());
            case "integer":
                long generatedLong = minimum + (long) (getRandom().nextDouble() * (maximum - minimum));
                return new LongGene(specPath, generatedLong);
            case "string":
            default:
                if (enumOptions != null && enumOptions.length != 0) {
                    // pick a random enum type
                    return new StringGene(specPath, enumOptions[getRandom().nextInt(enumOptions.length)]);
                } else if (pattern != null) {
                    // create a value from the pattern
                    return new StringGene(specPath, generateRandomValue(pattern));
                } else {
                    return new StringGene(specPath, generateRandomValue("[a-z]*"));
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
