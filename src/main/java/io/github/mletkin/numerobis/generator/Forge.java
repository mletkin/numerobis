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

import static io.github.mletkin.numerobis.common.Util.not;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.allMember;

import java.util.function.Predicate;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;

import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.generator.common.GenerationUtil;

/**
 * Do modifications inthe Builder Class.
 */
public class Forge {

    private static final String BUILDER_PACKAGE = "io.github.mletkin.numerobis";

    private CompilationUnit builderUnit;
    private ClassOrInterfaceDeclaration builderClass;

    private Forge(CompilationUnit builderUnit, ClassOrInterfaceDeclaration builderclass) {
        this.builderUnit = builderUnit;
        this.builderClass = builderclass;
    }

    public static Forge internal(CompilationUnit builderUnit, ClassOrInterfaceDeclaration productClass,
            String postfix) {
        return new Forge(builderUnit, createInternalBuilderClass(productClass, postfix));
    }

    public static Forge external(CompilationUnit builderUnit, String productClassName, String postfix) {
        return new Forge(builderUnit, createExternalBuilderClass(builderUnit, productClassName, postfix));
    }

    CompilationUnit builderUnit() {
        return builderUnit;
    }

    ClassOrInterfaceDeclaration builderClass() {
        return builderClass;
    }

    /**
     * Create the builder as member class.
     *
     * @param productclass
     *                                product class ot the builder
     * @param builderClassPostfix
     *                                the postfix for the Builder class name
     * @return the builder class description
     */
    private static ClassOrInterfaceDeclaration createInternalBuilderClass( //
            ClassOrInterfaceDeclaration productclass, String builderClassPostfix) {

        return allMember(productclass, ClassOrInterfaceDeclaration.class) //
                .filter(c -> c.getNameAsString().equals(builderClassPostfix)) //
                .findFirst() //
                .orElseGet(() -> newInternalBuilderClass(productclass, builderClassPostfix));
    }

    private static ClassOrInterfaceDeclaration newInternalBuilderClass(ClassOrInterfaceDeclaration productclass,
            String builderClassPostfix) {
        var memberClass = GenerationUtil.newMemberClass(builderClassPostfix);
        productclass.getMembers().add(memberClass);
        return memberClass;
    }

    /**
     * Create the builder as separate class.
     *
     * @param builderUnit
     *                             compilation unit for the builder class
     * @param productClassName
     *                             name of the production class
     * @param postfix
     *                             the postfix for the Builder class name
     * @return the builder class description
     */
    private static ClassOrInterfaceDeclaration createExternalBuilderClass(CompilationUnit builderUnit,
            String productClassName, String postfix) {
        return builderUnit.findAll(ClassOrInterfaceDeclaration.class).stream() //
                .filter(c -> c.getNameAsString().equals(productClassName + postfix)) //
                .filter(not(ClassOrInterfaceDeclaration::isInterface)) //
                .findFirst() //
                .orElseGet(() -> builderUnit.addClass(productClassName + postfix));
    }

    /**
     * Set the package declaration of the builder classes unit.
     *
     * @param packageDeclaration
     *                               package declaration to use
     */
    public void setPackageDeclaration(PackageDeclaration packageDeclaration) {
        if (!builderUnit.getPackageDeclaration().isPresent()) {
            builderUnit.setPackageDeclaration(packageDeclaration);
        }
    }

    /**
     * Use the import section of the product classes unit.
     *
     * @param productUnit
     *                        unit that keeps the product class
     */
    public void copyImports(CompilationUnit productUnit) {
        productUnit.getImports().stream() //
                .filter(Predicate.not(this::isBuilderImport)) //
                .forEach(builderUnit::addImport);
    }

    private boolean isBuilderImport(ImportDeclaration impDec) {
        return impDec.getNameAsString().startsWith(BUILDER_PACKAGE);
    }

    public boolean hasMatchingConstructor(ConstructorDeclaration productConstructor) {
        return allMember(builderClass, ConstructorDeclaration.class) //
                .anyMatch(cd -> ClassUtil.matchesParameter(cd, productConstructor));
    }

}
