package search.genes;

import search.Generator;

public abstract class Gene<T> {

    abstract T toJSON();

    abstract Gene mutate(Generator generator);

    abstract Gene<T> copy();

}
