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

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;

import io.github.mletkin.numerobis.generator.Facade;

class AccessorTest {

    @Test
    void accessMethodGenerated() {
        Assertions.assertThat(generateFromResource("Access")).isEqualTo(//
                "@AccessMethods" //
                        + "public class Access {" //
                        + "    int foo;" //
                        + "    public int foo() {" //
                        + "        return foo;" //
                        + "    }" //
                        + "}");
    }

    private String generateFromResource(String className) {
        try {
            return Facade.withAccessMethods(StaticJavaParser.parseResource(className + ".java"), className) //
                    .toString().replace("\r\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
