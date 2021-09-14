package util;

public final class Configuration {

    // - General
    public static final int POPULATION_SIZE = 50;
    public static final int TOURNAMENT_SIZE = 4;
    public static final int REQUESTS_GENERATOR_LIMIT = 5;

    public static final Long RECORDING_COVERAGE_TIME = (long) 5 * 60 * 1000; // 5 minutes

    // - Generator (generate chromosome)
    public static final double HTTP_METHOD_GET_PROB = 0.75;
    public static final double INCLUDE_PARAM_PROB = 0.25;
    public static final double SKIP_NONREQUIRED_KEY_PROB = 0.75;

    // - Fitness functions
    // Fitness with clustering
    public static final int NEW_CLUSTERS_AFTER_GEN = 10;
    // All fitness excl random
    public static final double ARCHIVE_THRESHOLD = 0.8;
    // Random
    public static final double ARCHIVE_THRESHOLD_RANDOM = 2.5;

    // - Similarity
    public static final double THRESHOLD_DIFFERENT_FEATURES = 0.5;

    // - Mutation of genes
    // Array gene
    public static final double REMOVE_ELEMENT_PROB = 0.1;
    public static final double ADD_ELEMENT_PROB = 0.1;
    // Boolean gene
    public static final double BOOL_FLIP_PROB = 0.95;
    // Object gene
    public static final double ADD_NONREQUIRED_CHILD_PROB = 0.1;
    public static final double REMOVE_CHILD_PROB = 0.1;
    public static final double REPLACE_CHILD_PROB = 0.2;
    // Long gene
    public static final double BOUNDARY_CASE_PROB = 0.15;
    // String gene
    public static final double FRACTION_STRING_TO_MUTATE = 0.2;
    public static final double OTHER_ENUM_PROB = 0.6;

    // Type mutation probabilities
    public static final double CHANGE_TYPE_PROB = 0.05;
    public static final boolean ADVANCED_TYPE_CHANGES = true; // use schema to generate new values (of different types)

    public static final double ADD_NEW_RANDOM_INDIVIDUAL = 0.01;

    // - Mutation operators
    public static final int MUTATIONS_PER_INDIVIDUAL = 3;
    public static final double MUTATE_HTTP_METHOD_PROB = 0.05;
    public static final double MUTATE_API_METHOD_PROB = 0.005;

    // - Crossover settings
    public static final boolean CROSSOVER_ENABLED = true;
    public static final CrossoverType CROSSOVER_TYPE = CrossoverType.RANDOM; // RANDOM, ONE_POINT, OR TWO_POINT
}

