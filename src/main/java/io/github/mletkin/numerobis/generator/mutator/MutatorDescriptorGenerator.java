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
package io.github.mletkin.numerobis.generator.mutator;

import java.util.Optional;
import java.util.stream.Stream;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import io.github.mletkin.numerobis.annotation.GenerateMutator;
import io.github.mletkin.numerobis.common.Util;
import io.github.mletkin.numerobis.generator.BuilderGenerator;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;
import io.github.mletkin.numerobis.generator.common.StringExtractor;

/**
 * Generator for mutator descriptor objects.
 * <p>
 * One declaration can contain more than one variable ( e.g. {@code int x,y;})
 */
public class MutatorDescriptorGenerator {
    private FieldDeclaration field;

    public MutatorDescriptorGenerator(FieldDeclaration field) {
        this.field = field;
    }

    /**
     * Produces a stream of method descriptors from a field declaration.
     *
     * @return Stream<MutatorMethodDescriptor>
     */
    public Stream<MutatorMethodDescriptor> stream() {
        return field.getVariables().stream() //
                .flatMap(this::toVariants);
    }

    private Stream<MutatorMethodDescriptor> toVariants(VariableDeclarator vd) {
        return Stream.of(map(vd, ListMutatorVariant.OBJECT));
    }

    private MutatorMethodDescriptor map(VariableDeclarator vd, ListMutatorVariant variant) {
        return new MutatorMethodDescriptor.Builder() //
                .withMethodName(methodName(vd)) //
                .withParameterName(vd.getNameAsString()) //
                .withParameterType(vd.getType()) //
                .withVariant(variant) //
                .build();
    }

    private String methodName(VariableDeclarator vd) {
        return customName().orElseGet(() -> standardMutatorName(vd));
    }

    private String standardMutatorName(VariableDeclarator vd) {
        return BuilderGenerator.MUTATOR_PREFIX + Util.firstLetterUppercase(vd.getNameAsString());
    }

    private Optional<String> customName() {
        return new StringExtractor(GenerateMutator.class, "name").value(field);
    }

}
