package util.config;

public final class Configuration {

    // - Arguments given to function
    public static int HEURISTIC = 1; // Default heuristic = randomFuzzer
    public static String SERVER = ""; // Default = rippled testnet

    public static BudgetType BUDGET_TYPE = BudgetType.GENERATION;
    public static long BUDGET = 1;
    // for time = 5 minutes
    // for evaluations = 5 evaluations
    // for generations = 5 generations

    public static CoverageIntervalType COVERAGE_INTERVAL_TYPE = CoverageIntervalType.GENERATION;
    public static int INTERVAL = 1;
    // for time = 1 minute
    // for evaluations = 1 evaluation
    // for generations = 1 generation

    public static boolean COVERAGE_CHECK = true;

    // - General
    public static final int POPULATION_SIZE = 50;
    public static final int REQUESTS_GENERATOR_LIMIT = 1;

    // - Generator (generate chromosome)
    public static final double HTTP_METHOD_GET_PROB = 0.1;
    public static final double INCLUDE_PARAM_PROB = 0.25;
    public static final double SKIP_NONREQUIRED_KEY_PROB = 0.75;

    // - Fitness functions
    // Fitness with clustering
    public static final int NEW_CLUSTERS_AFTER_GEN = 3;
    // All fitness excl random
    public static final double ARCHIVE_THRESHOLD = 0.8;
    // Random fuzzer
    public static double PROPORTION_MUTATED = 0.0;

    // - Similarity
    public static final double THRESHOLD_DIFFERENT_FEATURES = 0.5;

    // - Mutation of genes
    public static final double MUTATION_INSTEAD_OF_GENERATION = 0.9;
    // Array gene
    public static final double REMOVE_ELEMENT_PROB = 0.1;
    public static final double ADD_ELEMENT_PROB = 0.1;
    // Object gene
    public static final double ADD_NONREQUIRED_CHILD_PROB = 0.1;
    public static final double REMOVE_CHILD_PROB = 0.1;
    // Long gene
    public static final double BOUNDARY_CASE_PROB = 0.1;
    public static final boolean NO_OUTSIDE_BOUNDARY_CASES = false;
    // String gene
    public static final double FRACTION_STRING_TO_MUTATE = 0.2;
    public static final double OTHER_ENUM_PROB = 0.2;

    // Type mutation probabilities
    public static final boolean TYPE_CHANGES = true;
    public static double CHANGE_TYPE_PROB = 0.25; // 0, 0.1, 0.25, 0.5, 0.75, 0.9, 1.0
    public static final boolean ADVANCED_TYPE_CHANGES = true; // use schema to generate new values (of different types)

    public static final double ADD_NEW_RANDOM_INDIVIDUAL = 0.01;

    // - Mutation operators
    public static final int MUTATIONS_PER_INDIVIDUAL = 2; // 1 2 4 8 16 32
    public static final double MUTATE_HTTP_METHOD_PROB = 0.05; // 0.01, 0.05, 0.1
    public static final double MUTATE_API_METHOD_PROB = 0.005; // 0.001, 0.005, 0.01
    public static final double ADD_CHROMOSOME_PROP = 0.05;
    public static final double DELETE_CHROMOSOME_PROP = 0.05;


    // - Crossover settings
    public static final boolean CROSSOVER_ENABLED = false;
    public static final CrossoverType CROSSOVER_TYPE = CrossoverType.ONE_POINT; // RANDOM, ONE_POINT, OR TWO_POINT

    // - Test settings
    public static final int NUMBER_OF_ACCOUNTS = 3;

    // - Attempts allowed to run failed request
    public static final int MAX_ATTEMPTS = 0;

    public static final double SAMPLE_FROM_ARCHIVE = 0.0;

    public static final SelectionType SELECTION_TYPE = SelectionType.TOURNAMENT;
    public static final int TOURNAMENT_SIZE = 4; // 2 4 8 16 (only when using tournament)

}



