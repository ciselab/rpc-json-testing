package search.genes;

import search.Generator;
import openRPC.SchemaSpecification;
import util.config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomBool;

public class StringGene extends ValueGene<String> {

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

        if (getRandomBool(Configuration.MUTATION_INSTEAD_OF_GENERATION)) {

            // Option 1: always just create new stringGene because there are no chars to mutate
            if (this.getValue().length() == 0) {
                return getNewGene(generator);
            }

            // Option 2: pick one of the other enum values with a certain probability
            if (this.getSchema().getEnums() != null && (getRandomBool(Configuration.OTHER_ENUM_PROB) || !getSchema().isMutable())) {
                List<String> options = new ArrayList<>();
                Collections.addAll(options, getSchema().getEnums());

                if (options.contains(this.getValue())) {
                    int index = options.indexOf(this.getValue());
                    // Remove the current option from list
                    options.remove(index);
                }

                int numberOfOptions = options.size();
                int newValueIndex = getRandom().nextInt(numberOfOptions);

                return new StringGene(this.getSchema(), options.get(newValueIndex));
            }

            // Option 3: Mutate the chars
            String mutatedValue = mutateCharacters(this.getValue());

            return new StringGene(this.getSchema(), mutatedValue);

        } else {
            // Change gene into an entirely new value by generating new value
            return getNewGene(generator);
        }
    }

    public String mutateCharacters(String value) {
        // number of characters to change
        int toChange = (int) Math.ceil(Configuration.FRACTION_STRING_TO_MUTATE * value.length());

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

    @Override
    public StringGene copy() {
        return new StringGene(this.getSchema(), getValue());
    }

}
