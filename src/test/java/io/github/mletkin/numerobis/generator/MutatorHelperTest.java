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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;

import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.generator.mutator.MutatorMethodDescriptor;

class MutatorHelperTest {

    MutatorHelper mh = new MutatorHelper(generator());

    BuilderGenerator generator() {
        BuilderGenerator mock = Mockito.mock(BuilderGenerator.class);
        Mockito.when(mock.builderclass()).thenReturn(//
                ClassUtil.findClass(StaticJavaParser.parse(//
                        "class Foo{" //
                                + "Foo test(int[] x) {}" //
                                + "}"),
                        "Foo").get());
        Mockito.when(mock.builderClassType()).thenReturn(//
                new ClassOrInterfaceType("Foo"));

        return mock;
    }

    @Test
    void arrayMutatorIsFound() {
        MutatorMethodDescriptor mmd = //
                new MutatorMethodDescriptor.Builder().withMethodName("test") //
                        .withParameterName("para") //
                        .withParameterType(new ArrayType(PrimitiveType.intType())) //
                        .withVariant(ListMutatorVariant.OBJECT) //
                        .build();

        Assertions.assertThat(mh.hasMutator(mmd)).isTrue();
    }

}
