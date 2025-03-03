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

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that mutator methods shall be generated for a list field.
 * <p>
 * The annotation is ignored for non list fields.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(FIELD)
public @interface GenerateListMutator {

    enum Variant {

        /**
         * No mutator for the list field.
         */
        NONE,
        /**
         * Mutator with a List object as parameter.
         */
        OBJECT,
        /**
         * Mutator with vararg parameter list.
         */
        VARARG,
        /**
         * Mutator with a stream of items as parameter.
         */
        STREAM,
        /**
         * Mutator with a collection of items as parameter.
         */
        COLLECTION,

        ;
    }

    /**
     * Mutator variants that shall be generated.
     *
     * @return array of mutator variants
     */
    Variant[] variants();

    String name() default "";

}
