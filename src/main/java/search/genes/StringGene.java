package search.genes;

import search.Generator;
import search.openRPC.SchemaSpecification;
import search.openRPC.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomIndex;

public class StringGene extends ValueGene<String> {
    final private double FRACTION = 0.2;
    final private String REGEX_CHARACTERS = "\\^$.|?*+()[]{}";
    final private String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // TODO possibleChars should likely contain more options to cover a bigger scope
    final private String NUMBERS = "0123456789";
    final private String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public StringGene(SchemaSpecification schema, String value) {
        super(schema, value);
    }

    @Override
    public Gene mutate(Generator generator) {
        if (getSchema() == null) {
            return this.copy();
        }

        double probability = getRandom().nextDouble();

        if (probability < 0.95) {
            // Option 1: always just create new stringGene because there are no chars to mutate
            if (this.getValue().length() == 0) {
                return getNewGene(generator);
            }

            // Option 2: pick one of the other enum values with a certain probability
            if (this.getSchema().getEnums() != null && (probability < 0.6 || !getSchema().isMutable())) {
                List<String> options = new ArrayList<>();
                Collections.addAll(options, getSchema().getEnums());

                if (options.contains(this.getValue())) {
                    int index = options.indexOf(this.getValue());
                    // remove the current option from list
                    options.remove(index);
                }

                int numberOfOptions = options.size();
                int newValueIndex = getRandom().nextInt(numberOfOptions);

                return new StringGene(this.getSchema(), options.get(newValueIndex));
            }

            // Option 3: mutate Regex pattern and generate a new value
//            if (getSchema().getPattern() != null && probability < 0.3) {
//                // Note that the pattern in the schema is NOT changed
//                String regex = getSchema().getPattern();
//                String mutatedRegex = mutateRegex(regex);
//                return new StringGene(this.getSchema(), Generator.generateRandomValue(mutatedRegex));
//            }

            // Option 4: mutate characters in the string
//            if (this.getValue().equals("__ACCOUNT__")) {
//                return this;
//            }
            String mutatedValue = mutateCharacters(this.getValue());
            return new StringGene(this.getSchema(), mutatedValue);

        } else {
            // change gene schema type (e.g. no longer string but long, or still string but totally different string)
            return getNewGene(generator);
        }
    }

    public String mutateCharacters(String value) {
        // number of characters to change
        int toChange = (int) Math.ceil(FRACTION * value.length());

        // TODO length must always be higher than 0
        // select the position from which to start to change/add/delete a character
        int position = 0;
        if (value.length() > 1) {
            position = getRandom().nextInt(value.length() - toChange);
        }

        String mutatedValue = null;

        // convert the string to a character array
        char[] chars = value.toCharArray();

        // mutate a fraction of characters
        for (int i = 0; i < toChange; i++) {
            // generate a character
            char newChar = CHARS.charAt(getRandom().nextInt(CHARS.length()));

            // the current value of the string
            if (mutatedValue == null) {
                mutatedValue = value;
            } else {
                mutatedValue = mutatedValue;
            }

            double r = getRandom().nextDouble();
            double limit = (double) 1 / (double) 3;
            if (r < limit) {
                // replace character at the specified position in char array
                chars[i+position] = newChar;
                // convert the character array back into string
                mutatedValue = String.valueOf(chars);
            }
            else if (r > 2*limit) {
                // add character
                String leftSub = mutatedValue.substring(0, i+position);
                String rightSub = mutatedValue.substring(i+position, mutatedValue.length());
                mutatedValue = leftSub + newChar + rightSub;
                chars = mutatedValue.toCharArray();
                position += 1;
            } else {
                // delete character
                mutatedValue = mutatedValue.substring(0, i+position) + mutatedValue.substring(i+position+1, mutatedValue.length());
                chars = mutatedValue.toCharArray();
                position -= 1;
            }
        }
        return mutatedValue;
    }

    // DOES NOT WORK PROPERLY YET!
    public String mutateRegex(String regex) {
        // number of characters to change
        int toChange = (int) Math.ceil(FRACTION * regex.length());

        // select the position from which to start to change/add/delete a character
        int position = getRandom().nextInt(regex.length() - toChange);

        // convert the string to a character array
        char[] chars = regex.toCharArray();

        // TODO when the position is unfortunately chosen, the Regex expression is not mutated.
        String mutatedRegex = regex;
        for (int i = 0; i < toChange; i++) {
            // TODO replace NUMBER chars, but this can cause exceeding min/max bounds...
//            if (NUMBERS.contains("" + chars[i+position])) {
//                // generate a character
//                char newChar = NUMBERS.charAt(getRandom().nextInt(NUMBERS.length()));
//                // replace character at the specified position in char array
//                chars[i+position] = newChar;
//                // convert the character array back into string
//                mutatedRegex = String.valueOf(chars);
//            } else
            if (LETTERS.contains("" + chars[i+position])) {
                // generate a character
                char newChar = LETTERS.charAt(getRandom().nextInt(LETTERS.length()));
                // replace character at the specified position in char array
                chars[i+position] = newChar;
                // convert the character array back into string
                mutatedRegex = String.valueOf(chars);
            }
        }
        return mutatedRegex;
    }

    @Override
    public StringGene copy() {
        return new StringGene(this.getSchema(), getValue());
    }

}
