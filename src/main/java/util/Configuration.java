package util;

public final class Configuration {

    // - General
    private static int POPULATION_SIZE = 50;
    private static int REQUESTS_GENERATOR_LIMIT = 5;
    private static int TOURNAMENT_SIZE = 4;
    private static Long RECORDING_COVERAGE_TIME = (long) 5 * 60 * 1000;

    // - Generator
    private static double HTTP_METHOD_GET_PROB = 0.75;
    private static double INCLUDE_PARAM_PROB = 0.25;
    private static double SKIP_NONREQUIRED_KEY_PROB = 0.75;

    // - Fitness functions
    // All fitness excl random
    private static int NEW_CLUSTERS_AFTER_GEN = 10;
    private static double ARCHIVE_THRESHOLD = 0.8;
    // Random
    private static double ARCHIVE_THRESHOLD_RANDOM = 2.5;

    // - Similarity
    private static double THRESHOLD_DIFFERENT_FEATURES = 0.5;

    // - Mutation of genes
    private static double REMOVE_ELEMENT_PROB = 0.1;
    private static double ADD_ELEMENT_PROB = 0.1;
    private static double BOOL_FLIP_PROB = 0.95;
    private static double ADD_NONREQUIRED_CHILD_PROB = 0.1;
    private static double REMOVE_CHILD_PROB = 0.1;
    private static double REPLACE_CHILD_PROB = 0.2;
    private static double BOUNDARY_CASE_PROB = 0.15;
    private static double FRACTION_STRING_TO_MUTATE = 0.2;
    private static double OTHER_ENUM_PROB = 0.6;
    private static double CHANGE_TYPE_PROB = 0.2;

    // - Mutation operators
    private static int MUTATIONS_PER_INDIVIDUAL = 3;
    private static double MUTATE_HTTP_METHOD_PROB = 0.01;
    private static double MUTATE_API_METHOD_PROB = 0.99;

    public static int getPOPULATION_SIZE() {
        return POPULATION_SIZE;
    }

    public static int getREQUESTS_GENERATOR_LIMIT() {
        return REQUESTS_GENERATOR_LIMIT;
    }

    public static int getTOURNAMENT_SIZE() {
        return TOURNAMENT_SIZE;
    }

    public static Long getRECORDING_COVERAGE_TIME() {
        return RECORDING_COVERAGE_TIME;
    }

    public static double getHTTP_METHOD_GET_PROB() {
        return HTTP_METHOD_GET_PROB;
    }

    public static double getINCLUDE_PARAM_PROB() {
        return INCLUDE_PARAM_PROB;
    }

    public static double getSKIP_NONREQUIRED_KEY_PROB() {
        return SKIP_NONREQUIRED_KEY_PROB;
    }

    public static int getNEW_CLUSTERS_AFTER_GEN() {
        return NEW_CLUSTERS_AFTER_GEN;
    }

    public static double getARCHIVE_THRESHOLD() {
        return ARCHIVE_THRESHOLD;
    }

    public static double getARCHIVE_THRESHOLD_RANDOM() {
        return ARCHIVE_THRESHOLD_RANDOM;
    }

    public static int getMUTATIONS_PER_INDIVIDUAL() {
        return MUTATIONS_PER_INDIVIDUAL;
    }

    public static double getMUTATE_HTTP_METHOD_PROB() {
        return MUTATE_HTTP_METHOD_PROB;
    }

    public static double getMUTATE_API_METHOD_PROB() {
        return MUTATE_API_METHOD_PROB;
    }

    public static double getTHRESHOLD_DIFFERENT_FEATURES() {
        return THRESHOLD_DIFFERENT_FEATURES;
    }

    public static double getAddElementProb() {
        return ADD_ELEMENT_PROB;
    }

    public static double getRemoveElementProb() {
        return REMOVE_ELEMENT_PROB;
    }

    public static double getBOOL_FLIP_PROB() {
        return BOOL_FLIP_PROB;
    }

    public static double getAddNonrequiredChildProb() {
        return ADD_NONREQUIRED_CHILD_PROB;
    }

    public static double getRemoveChildProb() {
        return REMOVE_CHILD_PROB;
    }

    public static double getReplaceChildProb() {
        return REPLACE_CHILD_PROB;
    }

    public static double getBoundaryCaseProb() {
        return BOUNDARY_CASE_PROB;
    }

    public static double getFractionStringToMutate() {
        return FRACTION_STRING_TO_MUTATE;
    }

    public static double getOtherEnumProb() {
        return OTHER_ENUM_PROB;
    }

    public static double getChangeTypeProb() {
        return CHANGE_TYPE_PROB;
    }
}
