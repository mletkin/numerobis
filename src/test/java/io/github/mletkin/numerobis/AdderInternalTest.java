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

import static io.github.mletkin.numerobis.Util.internalWithConstructors;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Adder generation for generated internal builder.
 */
class AdderInternalTest {

    @Test
    void adderForListField() {
        assertThat(internalWithConstructors("WithList")).contains(//
                "public Builder addX(String item) {" //
                        + "        product.x.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void adderForListFieldWithPostfixS() {
        assertThat(internalWithConstructors("WithListWithPostfix")).contains(//
                "public Builder addProduct(String item) {" //
                        + "        product.products.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void adderForListFieldWithPostfixEn() {
        assertThat(internalWithConstructors("WithListWithPostfix")).contains(//
                "public Builder addPerson(String item) {" //
                        + "        product.personen.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void adderForListFieldWithPostfixE() {
        assertThat(internalWithConstructors("WithListWithPostfix")).contains(//
                "public Builder addBrief(String item) {" //
                        + "        product.briefe.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void adderForSetField() {
        assertThat(internalWithConstructors("WithSet")).contains(//
                "public Builder addX(String item) {" //
                        + "        product.x.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

}
