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
package io.github.mletkin.numerobis;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * General purpose utility methods.
 */
public final class Util {

    private Util() {
        // prevent instantiation
    }

    /**
     * negate the given predicate.
     * <p>
     * Prefix version of {@code Predicate.negate()}
     *
     * @param <T>
     *            predicate parameter class
     * @param predicate
     *            predicate to negate
     * @return the negated predicate as {@code Predicate}
     */
    static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    /**
     * Throws an exception if the flag evaluates to false.
     *
     * @param <T>
     *            Class of the exception to throw
     * @param flag
     *            flag to evaluate
     * @param supplier
     *            that produces the exception to be thrown
     * @throws T
     */
    static <T extends Throwable> void ifNotThrow(boolean flag, Supplier<T> supplier) throws T {
        if (!flag) {
            throw supplier.get();
        }
    }
}
