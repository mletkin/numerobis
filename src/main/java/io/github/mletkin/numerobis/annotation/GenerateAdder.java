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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controls the generation of adder methods.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface GenerateAdder {

    /**
     * Available adder variants.
     */
    enum Variant {

        /**
         * No adder for the list field.
         */
        NONE,
        /**
         * Adder with vararg parameter list.
         */
        VARARG,
        /**
         * Adder with a single item as parameter.
         */
        ITEM,
        /**
         * Adder with a stream of items as parameter.
         */
        STREAM,
        /**
         * Adder with a collection of items as parameter.
         */
        COLLECTION,

        ;
    }

    /**
     * Adder variants that shall be generated
     *
     * @return array of adder variants
     */
    Variant[] variants();

}
