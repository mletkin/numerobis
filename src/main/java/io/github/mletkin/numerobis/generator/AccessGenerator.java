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
import static io.github.mletkin.numerobis.generator.ClassUtil.allMember;
import static io.github.mletkin.numerobis.generator.GenerationUtil.nameExpr;
import static io.github.mletkin.numerobis.generator.GenerationUtil.returnStmt;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import io.github.mletkin.numerobis.annotation.Ignore;

/**
 * Generates access methods for a product class.
 */
public class AccessGenerator {

    private CompilationUnit unit;
    private ClassOrInterfaceDeclaration clazz;

    AccessGenerator(CompilationUnit unit, String className) {
        this.unit = unit;
        this.clazz = ClassUtil.findClass(unit, className).orElse(null);

        ifNotThrow(className != null, GeneratorException::productClassNotFound);
    }

    /**
     * Adds an aaccess method feald in the class.
     *
     * @return the generator instance
     */
    AccessGenerator addAccessMethods() {
        allMember(clazz, FieldDeclaration.class) //
                .filter(this::process) //
                .flatMap(fd -> new AccessMethodDescriptor.Generator(fd).stream()) //
                .forEach(this::addAccessorMethod);
        return this;
    }

    private boolean process(FieldDeclaration fd) {
        return (!fd.isAnnotationPresent(Ignore.class));
    }

    private AccessGenerator addAccessorMethod(AccessMethodDescriptor amd) {
        if (!hasAccessorMethod(amd)) {
            MethodDeclaration meth = clazz.addMethod(amd.methodName, Modifier.Keyword.PUBLIC);
            meth.setType(amd.returnType);
            meth.createBody() //
                    .addStatement(returnStmt(nameExpr(amd.fieldName)));
        }
        return this;
    }

    private boolean hasAccessorMethod(AccessMethodDescriptor amd) {
        return exists(//
                allMember(clazz, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(amd.methodName)) //
                        .filter(md -> md.getParameters().isEmpty()) //
                        .filter(md -> md.getType().equals(amd.returnType)));
    }

    /**
     * Returns the unit containing the class.
     *
     * @return the compilation unit with the modified class.
     */
    CompilationUnit resultUnit() {
        return unit;
    }
}
