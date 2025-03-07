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

import static io.github.mletkin.numerobis.generator.common.ClassUtil.allMember;
import static java.util.function.Predicate.not;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.generator.common.GenerationUtil;

/**
 * Produces the builder class.
 */
public final class Forge {

    private static final String BUILDER_PACKAGE = "io.github.mletkin.numerobis";

    private CompilationUnit builderUnit;
    private ClassOrInterfaceDeclaration builderClass;

    /**
     * Only to be called by factory methods.
     *
     * @param builderUnit  Unit to take the builder
     * @param builderclass Declaration of the builder class
     */
    private Forge(CompilationUnit builderUnit, ClassOrInterfaceDeclaration builderclass) {
        this.builderUnit = builderUnit;
        this.builderClass = builderclass;
    }

    /**
     * Creates a forge for an embedded builder for a product class.
     *
     * @param  builderUnit  unit to take the builder
     * @param  productClass name of the product class
     * @param  postfix      postfix of the builder class name
     * @return              the {@code Forge} instance
     */
    public static Forge internal(CompilationUnit builderUnit, ClassOrInterfaceDeclaration productClass,
            String postfix) {
        return new Forge(builderUnit, createInternalBuilderClass(productClass, postfix));
    }

    /**
     * Creates a forge for an embedded builder for a product record.
     *
     * @param  builderUnit   unit to take the builder
     * @param  productRecord name of the product record
     * @param  postfix       postfix of the builder class name
     * @return               the {@code Forge} instance
     */
    public static Forge internal(CompilationUnit builderUnit, RecordDeclaration productRecord, String postfix) {
        return new Forge(builderUnit, createInternalBuilderClass(productRecord, postfix));
    }

    /**
     * Creates a forge for a separate builder for a product class or record.
     *
     * @param  builderUnit      unit to take the builder
     * @param  productClassName name of the product class or record
     * @param  postfix          postfix of the builder class name
     * @return                  the {@code Forge} instance
     */
    public static Forge external(CompilationUnit builderUnit, String productClassName, String postfix) {
        return new Forge(builderUnit, createExternalBuilderClass(builderUnit, productClassName, postfix));
    }

    /**
     * Returns the compilation of the builder being forged.
     *
     * @return the {@link CompilationUnit} instance
     */
    public CompilationUnit builderUnit() {
        return builderUnit;
    }

    /**
     * Returns the type declaration of the builder class.
     *
     * @return the {@link ClassOrInterfaceDeclaration} instance
     */
    public ClassOrInterfaceDeclaration builderClass() {
        return builderClass;
    }

    /**
     * Creates the builder as member class.
     *
     * @param  productclass        product class ot the builder
     * @param  builderClassPostfix the postfix for the Builder class name
     * @return                     the builder class description
     */
    private static ClassOrInterfaceDeclaration createInternalBuilderClass( //
            ClassOrInterfaceDeclaration productclass, String builderClassPostfix) {

        return allMember(productclass, ClassOrInterfaceDeclaration.class) //
                .filter(c -> c.getNameAsString().equals(builderClassPostfix)) //
                .findFirst() //
                .orElseGet(() -> newInternalBuilderClass(productclass, builderClassPostfix));
    }

    private static ClassOrInterfaceDeclaration createInternalBuilderClass( //
            RecordDeclaration productclass, String builderClassPostfix) {

        return allMember(productclass, ClassOrInterfaceDeclaration.class) //
                .filter(c -> c.getNameAsString().equals(builderClassPostfix)) //
                .findFirst() //
                .orElseGet(() -> newInternalBuilderClass(productclass, builderClassPostfix));
    }

    private static ClassOrInterfaceDeclaration newInternalBuilderClass(TypeDeclaration productclass,
            String builderClassPostfix) {
        var memberClass = GenerationUtil.newMemberClass(builderClassPostfix);
        productclass.getMembers().add(memberClass);
        return memberClass;
    }

    /**
     * Creates the builder as separate class.
     *
     * @param  builderUnit      unit for the builder class
     * @param  productClassName name of the production class
     * @param  postfix          the postfix for the Builder class name
     * @return                  the builder class description
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
     * Sets the package declaration of the builder classes unit.
     *
     * @param packageDeclaration package declaration to use
     */
    public void setPackageDeclaration(PackageDeclaration packageDeclaration) {
        if (!builderUnit.getPackageDeclaration().isPresent()) {
            builderUnit.setPackageDeclaration(packageDeclaration);
        }
    }

    /**
     * Uses the import section of the product classes unit.
     *
     * @param productUnit unit that keeps the product class
     */
    public void copyImports(CompilationUnit productUnit) {
        productUnit.getImports().stream() //
                .filter(not(this::isBuilderImport)) //
                .forEach(builderUnit::addImport);
    }

    private boolean isBuilderImport(ImportDeclaration impDec) {
        return impDec.getNameAsString().startsWith(BUILDER_PACKAGE);
    }

    /**
     * Checks whether the builder contains a product constuctor matching the
     * declaration.
     *
     * @param  productConstructor the declaration of the expected constructor
     * @return                    {@code true} when a constructor is found
     */
    public boolean hasMatchingConstructor(ConstructorDeclaration productConstructor) {
        return allMember(builderClass, ConstructorDeclaration.class) //
                .anyMatch(cd -> ClassUtil.matchesParameter(cd, productConstructor));
    }

}
