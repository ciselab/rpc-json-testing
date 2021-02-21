package search.genes;

import search.Generator;
import search.openRPC.Specification;

import static util.RandomSingleton.getRandom;
import static util.RandomSingleton.getRandomIndex;

public class StringGene extends ValueGene<String> {

    public StringGene(String key, String value) {
        super(key, value);
    }

    @Override
    public Gene mutate(Generator generator) {
        if (getRandom().nextDouble() < 0.95) {
            // select position to change/add/delete a character

            if (this.getValue().length() == 0) {
                // TODO always just add because there are no chars to mutate
                return getNewGene(generator);
            }

            int position = getRandom().nextInt(this.getValue().length());

            // convert the string to a character array
            char[] chars = this.getValue().toCharArray();
            // generate a character
            // TODO: should possibleChars depend on valid input?
            String possibleChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            char newChar = possibleChars.charAt(getRandom().nextInt(possibleChars.length()));

            String mutatedValue;
            double r = getRandom().nextDouble();
            double limit = (double) 1 / (double) 3;
            if (r < limit) {
//                System.out.println("stringGene: char replaced");
                // replace character at the specified position in char array
                chars[position] = newChar;
                // convert the character array back into string
                mutatedValue = String.valueOf(chars);
            }
            else if (r > 2*limit) {
//                System.out.println("stringGene: char added");
                // add character
                String leftSub = getValue().substring(0, position);
                String rightSub = getValue().substring(position, getValue().length());
                mutatedValue = leftSub + newChar + rightSub;
            } else {
//                System.out.println("stringGene: char deleted");
                // delete character
                mutatedValue = getValue().substring(0, position) + getValue().substring(position+1, getValue().length());
            }

//            System.out.println("stringGene changed from " + this.getValue() + " to " + mutatedValue);

            return new StringGene(this.getSpecPath(), mutatedValue);
        } else {
            // change gene schema type (e.g. no longer string but long, or still string but totally different string)
            return getNewGene(generator);
        }
    }

    @Override
    public StringGene copy() {
        return new StringGene(this.getSpecPath(), getValue());
    }
}
