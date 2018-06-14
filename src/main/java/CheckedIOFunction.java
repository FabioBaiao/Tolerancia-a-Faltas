/** Represents a function that throws an IOException. */

import java.io.IOException;

@FunctionalInterface
public interface CheckedIOFunction<T, R> {
    R apply(T t) throws IOException;
}
