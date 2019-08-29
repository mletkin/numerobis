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

import static io.github.mletkin.numerobis.Util.externalWithConstructors;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * List field.
 */
class ListFieldTest {

    @Test
    void retainsAddMethod() {
        assertThat(externalWithConstructors("WithList", "public class WithListBuilder {" //
        + "    public WithListBuilder addX(String foo) {" //
        + "        return null;" //
        + "    }" //
        + "}") //
        ).isEqualTo(//
                "import java.util.List;" //
                        + "public class WithListBuilder {" // 1
                        + "    public WithListBuilder addX(String foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "    private WithList product;" //
                        + "    public WithListBuilder() {" //
                        + "        product = new WithList();" //
                        + "    }" //
                        + "    public WithListBuilder withX(List<String> x) {" //
                        + "        product.x = x;" //
                        + "        return this;" //
                        + "    }" //
                        + "    public WithList build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

}
