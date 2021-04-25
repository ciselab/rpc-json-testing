package search.objective;

import connection.Client;
import connection.ResponseObject;
import search.Generator;
import search.Individual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static util.RandomSingleton.getRandom;

/**
 * RandomFitness creates random fitness values for individuals (based on Gaussian distribution).
 */
public class RandomFitness extends Fitness {
    public RandomFitness(Client client) {
        super(client);
    }

    @Override
    public void evaluate(Generator generator, Individual individual) throws IOException {
        ResponseObject response = getClient().createRequest(individual.getHTTPMethod(), individual.toRequest());
//        System.out.println(response.getResponseCode() + " " + response.getResponseObject().toString());

//        getClient().createRequest(individual.getHTTPMethod(), individual.toRequest());
        individual.setFitness(getRandom().nextGaussian());
//        System.out.println(individual.getHTTPMethod()+ " " + individual.getMethod() + " " + individual.toRequest().toString());
    }

    @Override
    public void evaluate(Generator generator, List<Individual> population) {
        List<ResponseObject> responses = new ArrayList<>();

        double averageEvalTime = 0;

        for (int i = 0; i < population.size(); i++) {
            Individual individual = population.get(i);
            long start = System.nanoTime();
            try {
                responses.add(getClient().createRequest(individual.getHTTPMethod(), individual.toRequest()));
            } catch (IOException e) {
                e.printStackTrace();
            }

//            System.out.println("=====================");
//            System.out.println(individual.toRequest().toString());
//            System.out.println(responses.get(responses.size() - 1).getResponseObject().toString());
//            System.out.println("========");
            individual.setFitness(getRandom().nextGaussian());

            averageEvalTime += (System.nanoTime() - start);
        }

        averageEvalTime /= (population.size() * 1000000d);
        System.out.println("Average test time: " + averageEvalTime + " ms");

//        for (int i = 0; i < population.size(); i++) {
//            try {
//                evaluate(generator, population.get(i));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
