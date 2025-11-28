package io.th0rgal.oraxen.utils;

import org.jetbrains.annotations.NotNull;

public class Range {

    private final int lowerBound;
    private final int upperBound;

    public Range(int lowerBound, int upperBound) {
        this.lowerBound = Math.min(lowerBound, upperBound);
        this.upperBound = Math.max(lowerBound, upperBound);
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return lowerBound + ".." + upperBound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range range)) return false;
        return lowerBound == range.lowerBound && upperBound == range.upperBound;
    }

    @Override
    public int hashCode() {
        return 31 * lowerBound + upperBound;
    }
}
