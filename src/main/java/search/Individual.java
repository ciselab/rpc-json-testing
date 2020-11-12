package search;

import org.json.JSONObject;
import search.genes.ArrayGene;
import search.genes.JSONObjectGene;
import search.genes.StringGene;

public class Individual {
    private String method;
    private ArrayGene genes;

    public Individual() {
        // TODO: sample random individuals, for now use these hardcoded genes
        ArrayGene paramGenes = new ArrayGene();
        JSONObjectGene request_content = new JSONObjectGene();
        paramGenes.addChild(request_content);
        StringGene param_key1 = new StringGene("account");
        StringGene param_value1 = new StringGene("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
        StringGene param_key2 = new StringGene("ledger_index");
        StringGene param_value2 = new StringGene("validated");
        request_content.addChild(param_key1, param_value1);
        request_content.addChild(param_key2, param_value2);

        method = "account_info";
        genes = paramGenes;
    }

    public Individual(String method, ArrayGene genes) {
        this.method = method;
        this.genes = genes;
    }

    public JSONObject toReguest() {
        JSONObject request = new JSONObject();
        request.put("method", method);
        request.put("params", genes.toJSON());
        return request;
    }
}
