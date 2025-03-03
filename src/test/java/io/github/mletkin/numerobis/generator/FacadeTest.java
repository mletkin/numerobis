/**
 * (c) 2025 by Ullrich Rieger
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

import static io.github.mletkin.numerobis.Fixture.parse;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class FacadeTest {

    private static final Path RSCE = Path.of("src/test/resources");

    private Facade facade = new Facade(false);

    @Test
    void convertsClassWithField() {
        var product = parse("TestClass");

        var result = facade.withConstructors(product, "TestClass").execute();

        assertThat(RSCE.resolve("facade").resolve("EmbeddedWithConstructors.java")).hasContent(result.toString());
    }

}
