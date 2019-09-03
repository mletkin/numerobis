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
package io.github.mletkin.numerobis.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade.Result;

class ProcessorTest {

    static class Proc extends Processor {

        Proc() {
            super(new Order.Builder().withBuilderCreation(BuilderMojo.Creation.CONSTRUCTOR)
                    .withBuilderLocation(BuilderMojo.Location.SEPARATE).build());
        }

        @Override
        protected Optional<String> productTypeName(Destination dest) {
            return Optional.of("Access");
        }

        Result test() throws IOException {
            Destination dest = new Destination(new CompilationUnit(), Mockito.mock(Path.class)) //
                    .withProductUnit(StaticJavaParser.parseResource("Access.java"));

            return generate(dest);
        };
    }

    @Test
    void extenalBuilderNotPresent() throws IOException {
        new Proc().test();
    }
}
