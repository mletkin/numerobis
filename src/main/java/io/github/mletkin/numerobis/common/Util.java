/**
 * (c) 2019 by Ullrich Rieger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.mletkin.numerobis.common;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * General purpose utility methods.
 */
public final class Util {

    private Util() {
        // prevent instantiation
    }

    /**
     * Throws an exception if the flag evaluates to false.
     *
     * @param  <T>      Class of the exception to throw
     * @param  flag     flag to evaluate
     * @param  supplier that produces the exception to be thrown
     * @throws T        thrown if {@code flag} is {@code false}
     */
    public static <T extends Throwable> void ifNotThrow(boolean flag, Supplier<T> supplier) throws T {
        if (!flag) {
            throw supplier.get();
        }
    }

    /**
     * Checks whether a stream contains an object.
     *
     * @param  stream Stream to checkxs
     * @return        {@code true} if the stream is not empty
     */
    public static boolean exists(Stream<?> stream) {
        return stream.findAny().isPresent();
    }

    /**
     * Returns a stream from a collection, {@code null} save.
     *
     * @param  <T>        class of the objects in the collection
     * @param  collection collection of objeccts
     * @return            stream of collection objects
     */
    public static <T> Stream<T> stream(Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }

    /**
     * Creates all non existing directories on the path of a file.
     *
     * @param destinationFile file with path
     */
    public static void createParentPath(Path destinationFile) {
        var parent = destinationFile.getParent().toFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
    }

    /**
     * Converts the first letter of a string to uppercase.
     *
     * @param  str string to process
     * @return     processed string
     */
    public static String firstLetterUppercase(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Returns the first non empty array in a list.
     *
     * @param  <T>  item type of the arrays
     * @param  list list of arrays
     * @return      the first non empty array in the list, wrapped in an optional
     */
    public static <T> Optional<T[]> firstNotEmpty(T[]... list) {
        return Stream.of(list) //
                .filter(Objects::nonNull) //
                .filter(a -> a.length > 0) //
                .findFirst();
    }

    /**
     * Checks whether a string is {@code null} or a string of whitespace.
     *
     * @param  str {@code String} to check
     * @return     {@code true} when the string is {@code null} or blank
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

}
