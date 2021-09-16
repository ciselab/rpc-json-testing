package objective;

public enum Type {
    // String
    EMPTY_STRING,
    SHORT_STRING,
    LONG_STRING,

    // Number
    FLOAT_POSITIVE,
    FLOAT_NEGATIVE,
    FLOAT_ZERO,
    DOUBLE_POSITIVE,
    DOUBLE_NEGATIVE,
    DOUBLE_ZERO,
    INTEGER_POSITIVE,
    INTEGER_NEGATIVE,
    INTEGER_ZERO,
    LONG_POSITIVE,
    LONG_NEGATIVE,
    LONG_ZERO,

    // Boolean
    TRUE_BOOLEAN,
    FALSE_BOOLEAN,

    // Array
    EMPTY_ARRAY,
    STRING_ARRAY,
    NUMBER_ARRAY,
    BOOLEAN_ARRAY,
    NULL_ARRAY,

    // Special
    NULL
    ;

    /**
     * Match the type of a value to a prespecified category.
     * @param value
     * @return String category the value belongs to
     */
    public static Type matchType(Object value) {
        if (value instanceof String) {
            return matchStringType((String) value);
        } else if (value instanceof Number) {
            return matchNumberType((Number) value);
        } else if (value instanceof Boolean) {
            if ((Boolean) value) {
                return TRUE_BOOLEAN;
            } else {
                return FALSE_BOOLEAN;
            }
        }

        throw new IllegalArgumentException("Given value type is not yet implemented! " + value);
    }

    public static Type matchTypeArray(Object value) {
        if (value instanceof String) {
            return STRING_ARRAY;
        } else if (value instanceof Number) {
            return NUMBER_ARRAY;
        } else if (value instanceof Boolean) {
            return BOOLEAN_ARRAY;
        }

        throw new IllegalArgumentException("Given value type is not yet implemented! " + value);
    }

    private static Type matchStringType(String value) {
        int threshold = 10;
        if (value.equals("")) {
            return EMPTY_STRING;
        } else if (value.length() <= threshold) {
            return SHORT_STRING;
        } else if (value.length() > threshold) {
            return LONG_STRING;
        }
        throw new IllegalArgumentException("Given string type is not yet implemented! " + value);
    }

    private static Type matchNumberType(Number value) {
        if (value instanceof Float) {
            return matchFloatType((Float) value);
        } else if (value instanceof Double) {
            return matchDoubleType((Double) value);
        } else if (value instanceof Integer) {
            return matchIntegerType((Integer) value);
        } else if (value instanceof Long) {
            return matchLongType((Long) value);
        }
        throw new IllegalArgumentException("Given number type is not yet implemented! " + value);
    }

    private static Type matchFloatType(Float value) {
        if (value == 0f) {
            return FLOAT_ZERO;
        } else if (value > 0f) {
            return FLOAT_POSITIVE;
        } else {
            return FLOAT_NEGATIVE;
        }
    }

    private static Type matchDoubleType(Double value) {
        if (value == 0d) {
            return DOUBLE_ZERO;
        } else if (value > 0d) {
            return DOUBLE_POSITIVE;
        } else {
            return DOUBLE_NEGATIVE;
        }
    }

    private static Type matchIntegerType(Integer value) {
        if (value == 0) {
            return INTEGER_ZERO;
        } else if (value > 0) {
            return INTEGER_POSITIVE;
        } else {
            return INTEGER_NEGATIVE;
        }
    }

    private static Type matchLongType(Long value) {
        if (value == 0l) {
            return LONG_ZERO;
        } else if (value > 0l) {
            return LONG_POSITIVE;
        } else {
            return LONG_NEGATIVE;
        }
    }
}
