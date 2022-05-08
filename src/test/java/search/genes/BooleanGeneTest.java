package search.genes;

import openRPC.SchemaSpecification;
import openRPC.Specification;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import search.Generator;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static util.IO.readFile;

class BooleanGeneTest {

    public static SchemaSpecification schemaSpecification;
    public static BooleanGene booleanGene;
    public static Generator generator;

    @BeforeAll
    public static void prepareTest() {
        // Read the OpenRPC specification that will be used
        File jar = new File(BooleanGeneTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String directory = jar.getParentFile().getAbsolutePath();
        String filepath;
        filepath = directory + System.getProperty("file.separator") + "test-openrpc.json";
        Specification specification = null;
        try {
            String data = readFile(filepath);
            specification = new Specification(new JSONObject(data));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Retrieve schema of the boolean parameter Strict
        generator = new Generator(specification);
        schemaSpecification = specification.getSchemas().get("#/components/contentDescriptors/Strict/schema").get(0);
        booleanGene = new BooleanGene(schemaSpecification, true);
    }

    /**
     * BooleanGene mutate method should always change the gene.
     */
    @Test
    public void mutateTest() {
        Gene mutatedBg = booleanGene.mutate(generator);
        assertNotEquals(mutatedBg, booleanGene);
        assertNotEquals(mutatedBg, mutatedBg.mutate(generator));
    }
}