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
package io.github.mletkin.numerobis.generator;

/**
 * Combined list adder/mutator variants for the generator.
 */
public enum ListMutatorVariant {

    /**
     * No mutator or adder for the list field.
     */
    NONE,
    /**
     * Mutator only:<br>
     * Pass the object.
     */
    OBJECT,
    /**
     * Pass items as vararg parameter list.
     */
    VARARG,
    /**
     * Adder only:<br>
     * Pass a single item.
     */
    ITEM,
    /**
     * Pass the items as stream.
     */
    STREAM,
    /**
     * Pass the items as collection
     */
    COLLECTION,

    ;

    /**
     * Checks whether the instance is the var arg variant.
     *
     * @return {@code true} if the instance is the var arg variant
     */
    public boolean isVarArg() {
        return this == VARARG;
    }

}
