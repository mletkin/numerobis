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

    public enum Variant {
        NONE, // no adder for the list field
        OBJECT, // pass complete object -- non adder use only
        VARARG, // pass items as vararg parameter list
        ITEM, // pass a single item
        STREAM, // pass items as stream
        COLLECTION, // pass items as collection
        ;
    }

    Variant[] listVariants();
}
