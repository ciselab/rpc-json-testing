package util;

public class Configuration {

    // - General
    private int POPULATION_SIZE;
    private int REQUESTS_GENERATOR_LIMIT;
    private int TOURNAMENT_SIZE;
    private Long RECORDING_COVERAGE_TIME;

    // - Generator
    private double HTTP_METHOD_GET_PROB;
    private double INCLUDE_PARAM_PROB;
    private double SKIP_NONREQUIRED_KEY_PROB;

    // - Fitness functions
    // All fitness excl random
    private int NEW_CLUSTERS_AFTER_GEN;
    private double ARCHIVE_THRESHOLD;
    // Random
    private double ARCHIVE_THRESHOLD_RANDOM;

    // - Mutation operators
    private int MUTATIONS_PER_INDIVIDUAL;
    private double MUTATE_HTTP_METHOD_PROB;
    private double MUTATE_API_METHOD_PROB;

    public Configuration() {
        this.POPULATION_SIZE = 50;
        this.REQUESTS_GENERATOR_LIMIT = 5;
        this.TOURNAMENT_SIZE = 4;
        this.RECORDING_COVERAGE_TIME = (long) 5 * 60 * 1000;

        this.HTTP_METHOD_GET_PROB = 0.75;
        this.INCLUDE_PARAM_PROB = 0.25;
        this.SKIP_NONREQUIRED_KEY_PROB = 0.75;

        this.NEW_CLUSTERS_AFTER_GEN = 10;
        this.ARCHIVE_THRESHOLD = 0.8;
        this.ARCHIVE_THRESHOLD_RANDOM = 2.5;

        this.MUTATIONS_PER_INDIVIDUAL = 3;
        this.MUTATE_HTTP_METHOD_PROB = 0.01;
        this.MUTATE_API_METHOD_PROB = 0.99;
    }

    public int getPOPULATION_SIZE() {
        return POPULATION_SIZE;
    }

    public int getREQUESTS_GENERATOR_LIMIT() {
        return REQUESTS_GENERATOR_LIMIT;
    }

    public int getTOURNAMENT_SIZE() {
        return TOURNAMENT_SIZE;
    }

    public Long getRECORDING_COVERAGE_TIME() {
        return RECORDING_COVERAGE_TIME;
    }

    public double getHTTP_METHOD_GET_PROB() {
        return HTTP_METHOD_GET_PROB;
    }

    public double getINCLUDE_PARAM_PROB() {
        return INCLUDE_PARAM_PROB;
    }

    public double getSKIP_NONREQUIRED_KEY_PROB() {
        return SKIP_NONREQUIRED_KEY_PROB;
    }

    public int getNEW_CLUSTERS_AFTER_GEN() {
        return NEW_CLUSTERS_AFTER_GEN;
    }

    public double getARCHIVE_THRESHOLD() {
        return ARCHIVE_THRESHOLD;
    }

    public double getARCHIVE_THRESHOLD_RANDOM() {
        return ARCHIVE_THRESHOLD_RANDOM;
    }

    public int getMUTATIONS_PER_INDIVIDUAL() {
        return MUTATIONS_PER_INDIVIDUAL;
    }

    public double getMUTATE_HTTP_METHOD_PROB() {
        return MUTATE_HTTP_METHOD_PROB;
    }

    public double getMUTATE_API_METHOD_PROB() {
        return MUTATE_API_METHOD_PROB;
    }
}
