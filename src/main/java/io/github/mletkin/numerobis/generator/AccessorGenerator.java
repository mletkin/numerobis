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

import static io.github.mletkin.numerobis.common.Util.exists;
import static io.github.mletkin.numerobis.common.Util.ifNotThrow;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.allMember;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.methodCall;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.nameExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.returnStmt;
import static java.util.function.Predicate.not;

import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.annotation.GenerateAccessors;
import io.github.mletkin.numerobis.annotation.Ignore;
import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.generator.common.GenerationUtil;
import io.github.mletkin.numerobis.generator.common.StringExtractor;

/**
 * Generates access methods for a product class.
 * <p>
 * Might use a prefix from the {@link GenerateAccessors} annotation.
 */
public class AccessorGenerator {

    private CompilationUnit unit;
    private ClassOrInterfaceDeclaration clazz;
    private String prefix;

    /**
     * Creates a {@code AccessorGenerator} instance.
     *
     * @param unit      thr compilation unit with the class to modify
     * @param className Name of the class to modify
     */
    AccessorGenerator(CompilationUnit unit, String className) {
        this.unit = unit;
        this.clazz = ClassUtil.findClass(unit, className).orElse(null);
        this.prefix = new StringExtractor(GenerateAccessors.class, "prefix").value(clazz).orElse("");

        ifNotThrow(className != null, GeneratorException::productClassNotFound);
    }

    /**
     * Adds an accessor for every variable in every field declaration.
     *
     * @return the generator instance
     */
    public AccessorGenerator addAccessors() {
        allMember(clazz, FieldDeclaration.class) //
                .filter(this::process) //
                .flatMap(fd -> new AccessorMethodDescriptor.Generator(fd, prefix, unit).stream()) //
                .filter(not(this::hasAccessorMethod)) //
                .forEach(this::addAccessor);
        return this;
    }

    private boolean process(FieldDeclaration fd) {
        return !fd.isAnnotationPresent(Ignore.class);
    }

    private void addAccessor(AccessorMethodDescriptor amd) {
        MethodDeclaration meth = clazz.addMethod(amd.methodName, Modifier.Keyword.PUBLIC);
        if (amd.streamAccessor) {
            meth.setType(streamType(amd));
            meth.createBody() //
                    .addStatement(returnStmt(methodCall(nameExpr(amd.fieldName), "stream")));
            unit.addImport(Stream.class);
        } else {
            meth.setType(amd.fieldType);
            meth.createBody() //
                    .addStatement(returnStmt(nameExpr(amd.fieldName)));
        }
    }

    private Type streamType(AccessorMethodDescriptor amd) {
        Type argType = amd.fieldType.asClassOrInterfaceType().getTypeArguments().get().get(0);
        return GenerationUtil.streamType(argType);
    }

    private boolean hasAccessorMethod(AccessorMethodDescriptor amd) {
        return exists(//
                allMember(clazz, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(amd.methodName)) //
                        .filter(md -> md.getParameters().isEmpty()) //
                        .filter(md -> md.getType().equals(amd.streamAccessor ? streamType(amd) : amd.fieldType)));
    }

    /**
     * Returns the unit containing the class.
     *
     * @return the compilation unit with the modified class.
     */
    public CompilationUnit resultUnit() {
        return unit;
    }
}
