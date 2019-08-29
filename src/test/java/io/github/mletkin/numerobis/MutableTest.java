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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MutableTest {

    @Test
    void externalManipulationConstructorIsRetained() {
        assertThat(Util.externalWithConstructors("Mutable", //
                "public class MutableBuilder {" //
                        + "    public MutableBuilder(Mutable item) {" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
                "public class MutableBuilder {" //
                        + "    public MutableBuilder(Mutable item) {" //
                        + "    }" //
                        + "    private Mutable product;" //
                        + "    public MutableBuilder() {" //
                        + "        product = new Mutable();" //
                        + "    }" //
                        + "    public Mutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void externalManipulationFactoryIsRetained() {
        assertThat(Util.externalWithFactories("Mutable", //
                "public class MutableBuilder {" //
                        + "    public static MutableBuilder of(Mutable item) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
                "public class MutableBuilder {" //
                        + "    public static MutableBuilder of(Mutable item) {" //
                        + "        return null;" //
                        + "    }" //
                        + "    private Mutable product;" //
                        + "    private MutableBuilder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }" //
                        + "    public static MutableBuilder of() {" //
                        + "        return new MutableBuilder(new Mutable());" //
                        + "    }" //
                        + "    public Mutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

}
