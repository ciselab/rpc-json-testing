package search.genes;

import openRPC.ResultSpecification;
import openRPC.SchemaSpecification;
import org.json.JSONObject;
import search.Generator;
import util.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static util.RandomSingleton.getRandom;

public class MethodGene extends Gene<JSONObject> {

    private String httpMethod;
    private String apiMethod;
    private ArrayGene body;
    private ResultSpecification resultSchema;

    public MethodGene(String httpMethod, String apiMethod, ArrayGene body, ResultSpecification resultSchema) {
        super(null);
        this.httpMethod = httpMethod;
        this.apiMethod = apiMethod;
        this.body = body;
        this.resultSchema = resultSchema;
    }

    @Override
    public JSONObject toJSON(Map<MethodGene, JSONObject> previousResponse) {
        JSONObject request = new JSONObject();
        request.put("method", apiMethod);
        request.put("params", this.body.toJSON(previousResponse));
        return request;
    }

    @Override
    public MethodGene mutate(Generator generator) {
        double choice = getRandom().nextDouble();
        if (choice < Configuration.MUTATE_HTTP_METHOD_PROB) {
            // mutate http apiMethod
            return new MethodGene(generator.generateHTTPMethod(), apiMethod, this.body.copy(), this.resultSchema);
        } else {
            // mutate parameters of api apiMethod
            ArrayGene newGenes = this.body.mutate(generator);
            return new MethodGene(httpMethod, apiMethod, newGenes, this.resultSchema);
        }
    }

    @Override
    public Gene<JSONObject> copy() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public List<Gene> getChildren() {
        List<Gene> children = new ArrayList<>();

        children.add(this.body);

        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MethodGene that = (MethodGene) o;
        return
//            Objects.equals(httpMethod, that.httpMethod) &&
                Objects.equals(apiMethod, that.apiMethod) &&
                        Objects.equals(body, that.body);
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public ArrayGene getBody() {
        return body;
    }

    public ResultSpecification getResultSchema() {
        return resultSchema;
    }
}
