//package search;
//
//import com.github.curiousoddman.rgxgen.RgxGen;
//import search.genes.ArrayGene;
//import search.genes.BooleanGene;
//import search.genes.Gene;
//import search.genes.LongGene;
//import search.genes.JSONObjectGene;
//import search.genes.StringGene;
//
//import java.util.Random;
//
//public class OldGenerator {
//
//    private RPCSpecification specification;
//    // TODO: create singleton random
//    private Random random;
//
//    public OldGenerator(RPCSpecification specification) {
//        this.specification = specification;
//        this.random = new Random();
//    }
//
//    public Individual generateIndividual() {
//        // get random method
//        Method method = specification.getMethods().get(random.nextInt(specification.getMethods().size()));
//
//        return generateIndividual(method);
//    }
//
//    public Individual generateIndividual(Method method) {
//
//        JSONObjectGene objectGene = new JSONObjectGene();
//
//        for (Parameter parameter : method.getParameters()) {
//            if (parameter.isRequired()) {
//                objectGene.addChild(new StringGene(parameter.getName()), generateGene(parameter));
//            } else if (random.nextDouble() > 0.5) { // if not required 50% chance to add it.
//                objectGene.addChild(new StringGene(parameter.getName()), generateGene(parameter));
//            }
//        }
//
//        ArrayGene arrayGene = new ArrayGene();
//
//        arrayGene.addChild(objectGene);
//
//        return new Individual(generateHTTPMethod(), method.getName(), arrayGene);
//    }
//
//    public String generateHTTPMethod() {
//        String[] methods = new String[]{"POST", "GET"};
//        String method = methods[0];
//        if (random.nextDouble() > 0.75) {
//            method = methods[1];
//        }
//        return method;
//    }
//
//    public Gene generateGene(Parameter parameter) {
//        // TODO: boolean, object, (array)
//        // randomly pick one of the types
//        Type type = parameter.getTypes().get(random.nextInt(parameter.getTypes().size()));
//
//        if (parameter.getName().equals("account")) {
//            return new StringGene("rKyn3hKyvwmnjuF4MxBrJQA9dxPL7g4ZgA");
//        }
//
//        return generateGene(type);
//    }
//
//    public Gene generateGene(Type type) {
//        switch (type.getType()) {
//            case "object":
//                //TODO: maybe change later
//                return new JSONObjectGene();
//            case "boolean":
//                return new BooleanGene(random.nextBoolean());
//            case "integer":
//                Long min = 0L;
//                Long max = Long.MAX_VALUE;
//
//                if (type.getMinimum() != null) {
//                    min = type.getMinimum();
//                }
//                if (type.getMaximum() != null) {
//                    max = type.getMaximum();
//                }
//
//                long generatedLong = min + (long) (random.nextDouble() * (max - min));
//
//                return new LongGene(generatedLong);
//            case "string":
//            default:
//                if (!type.getEnumOptions().isEmpty()) {
//                    // pick a random enum type
//                    return new StringGene(type.getEnumOptions().get(random.nextInt(type.getEnumOptions().size())));
//                } else if (type.getPattern() != null) {
//                    // create a value from the pattern
//                    return new StringGene(generateRandomValue(type.getPattern()));
//                }
//        }
//
//        throw new IllegalArgumentException("Invalid parameter ");
//    }
//
//    public String generateRandomValue(String regex) {
//        //TODO: investigate rgxGen lib and alternatives
//
//        RgxGen rgxGen = new RgxGen(regex);
//
//        return rgxGen.generate(random);
//    }
//}
