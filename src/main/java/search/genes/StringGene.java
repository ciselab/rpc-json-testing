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

            if (this.getValue().length() == 0) {
                // always just create new stringGene because there are no chars to mutate
                return getNewGene(generator);
            }

            // pick one of the other enum values with a certain probability
            if (this.getSchema().getEnums() != null && probability < 0.75) {
                List<String> options = new ArrayList<>();
                Collections.addAll(options, getSchema().getEnums());

                if (options.contains(this.getValue())) {
                    int index = options.indexOf(this.getValue());
                }

                int numberOfOptions = options.size();
                int newValueIndex = getRandom().nextInt(numberOfOptions);

                return new StringGene(this.getSchema(), options.get(newValueIndex));
            }

            // select position to change/add/delete a character
            int position = getRandom().nextInt(this.getValue().length());

            // TODO possibleChars should likely contain more options to cover a bigger scope and create invalid strings?
            // TODO should it be possible to do more than one mutation?

            // convert the string to a character array
            char[] chars = this.getValue().toCharArray();
            // generate a character
            String possibleChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            char newChar = possibleChars.charAt(getRandom().nextInt(possibleChars.length()));

            String mutatedValue;
            double r = getRandom().nextDouble();
            double limit = (double) 1 / (double) 3;
            if (r < limit) {
                // replace character at the specified position in char array
                chars[position] = newChar;
                // convert the character array back into string
                mutatedValue = String.valueOf(chars);
            }
            else if (r > 2*limit) {
                // add character
                String leftSub = getValue().substring(0, position);
                String rightSub = getValue().substring(position, getValue().length());
                mutatedValue = leftSub + newChar + rightSub;
            } else {
                // delete character
                mutatedValue = getValue().substring(0, position) + getValue().substring(position+1, getValue().length());
            }
            return new StringGene(this.getSchema(), mutatedValue);
        } else {
            // change gene schema type (e.g. no longer string but long, or still string but totally different string)
            return getNewGene(generator);
        }
    }

    @Override
    public StringGene copy() {
        return new StringGene(this.getSchema(), getValue());
    }

}
