package search.genes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringGeneTest {

    @Test
    public void mutateRegexTest() throws Exception
    {
        StringGene sg = new StringGene(null, null);
        String originalRegex = "$abc584";
        String mutatedRegex = sg.mutateRegex(originalRegex);
        assertTrue(!mutatedRegex.equals(originalRegex));
        System.out.println(mutatedRegex);
    }
}