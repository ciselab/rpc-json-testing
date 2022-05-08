package search;

import openRPC.ParamSpecification;
import openRPC.SchemaSpecification;
import openRPC.Specification;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import search.genes.ArrayGene;
import search.genes.Gene;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static util.IO.readFile;

class GeneratorTest {

    public static Generator generator;
    public static Specification specification;

    @BeforeAll
    public static void prepareTest() {
        // Read the OpenRPC specification that will be used
        File jar = new File(GeneratorTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String directory = jar.getParentFile().getAbsolutePath();
        String filepath;
        filepath = directory + System.getProperty("file.separator") + "test-openrpc.json";
        try {
            String data = readFile(filepath);
            specification = new Specification(new JSONObject(data));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create generator
        generator = new Generator(specification);
    }

    @Test
    public void getRandomMethodTest() {
        String method = generator.getRandomMethod();
        // Specific for this test-openrpc file
        assertEquals("account_channels", method);
    }

    @Test
    public void generateMethodTest() {
        String method = generator.getRandomMethod();

        List<ParamSpecification> params = specification.getMethods().get(method);

        // Specific for this test-openrpc file
        assertEquals(true, params.get(0).isRequired());
        assertEquals(6, params.size());

        ArrayGene arrayGene = generator.generateMethod(method);

        assertEquals(true, arrayGene.hasChildren());
        assertTrue(arrayGene.toJSON().toString().contains("account"));
        assertTrue(arrayGene.toJSON().toString().contains("__ACCOUNT__"));

        for (ParamSpecification param : params) {
            if (arrayGene.toJSON().toString().contains(param.getName())) {

                List<SchemaSpecification> schemaOptions = specification.getSchemas().get(param.getPath());

                for( SchemaSpecification schema : schemaOptions) {
                    System.out.println(param.getName() + " should be of type: " + schema.getType());
                }
            }
        }

        System.out.println(arrayGene.toJSON().toString());

    }

    @Test
    public void generateValueGeneTest() {
        String method = generator.getRandomMethod();

        List<ParamSpecification> params = specification.getMethods().get(method);

        SchemaSpecification schema = specification.getSchemas().get(params.get(0).getPath()).get(0);

        Gene gene = generator.generateValueGene(schema);

        assertEquals(gene.getSchema().getType(), schema.getType());
    }
}