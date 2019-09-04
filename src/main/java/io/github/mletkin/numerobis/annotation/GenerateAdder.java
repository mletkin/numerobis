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
package io.github.mletkin.numerobis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Controls the generation of adder methods.
 * <p>
 * Currently only the enum is used.
 */
@Target(ElementType.FIELD)
public @interface GenerateAdder {

    /**
     * Available adder variants.
     */
    public enum Variant {
        NONE, // no adder for the list field
        VARARG, // adder with vararg parameter list
        ITEM, // adder with a single item as parameter
        STREAM, // adder with a stream of items as parameter
        COLLECTION, // adder with a collection of items as parameter
        ;
    }

    /**
     * Adder variants that should be generated
     *
     * @return array of adder variants
     */
    Variant[] variants();
}
