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

import static io.github.mletkin.numerobis.Fixture.asString;
import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static io.github.mletkin.numerobis.Fixture.parseString;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.numerobis.generator.Facade;

class MutableTest {

    private Facade facade = new Facade();

    @ParameterizedTest
    @MethodSource("testCases")
    void mergeTest(String desc, String product, String builder, String expected) {
        var order = mkOrder(product).useBuildUnit(parseString(builder));

        var actual = facade.separateWithConstructors(order).execute();

        assertThat(asString(actual)).as(desc).isEqualTo(expected);

    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("externalManipulationConstructorIsRetained", "Mutable", //
                        "public class MutableBuilder {" //
                                + "    public MutableBuilder(Mutable item) {" //
                                + "    }" //
                                + "}", //
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
                                + "}")

        );
    }

    @Test
    void externalManipulationFactoryIsRetained() {
        var product = "Mutable";
        var builder = "public class MutableBuilder {" //
                + "    public static MutableBuilder of(Mutable item) {" //
                + "        return null;" //
                + "    }" //
                + "}"; //
        var order = mkOrder(product).useBuildUnit(parseString(builder));

        var actual = facade.separateWithFactoryMethods(order).execute();

        assertThat(asString(actual)).isEqualTo( //
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
