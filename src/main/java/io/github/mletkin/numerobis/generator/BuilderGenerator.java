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
import static io.github.mletkin.numerobis.common.Util.not;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.allMember;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.hasDefaultConstructor;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.hasExplicitConstructor;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.hasProductConstructor;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.args;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.assignExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.fieldAccess;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.nameExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.newExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.returnStmt;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.thisExpr;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import io.github.mletkin.numerobis.annotation.Ignore;
import io.github.mletkin.numerobis.annotation.Immutable;
import io.github.mletkin.numerobis.annotation.Mutable;
import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.generator.common.GenerationUtil;
import io.github.mletkin.numerobis.generator.mutator.ListMutatorDescriptorGenerator;
import io.github.mletkin.numerobis.generator.mutator.MutatorDescriptorGenerator;
import io.github.mletkin.numerobis.generator.mutator.MutatorMethodDescriptor;
import io.github.mletkin.numerobis.plugin.Naming;

/**
 * Generates builder classes product classes in a seperate compilation unit.
 */
public class BuilderGenerator {

    private static final String BUILDER_PACKAGE = "io.github.mletkin.numerobis";
    final static String FIELD = "product";
    public final static String CLASS_POSTFIX = "Builder";

    final static String ADDER_PREFIX = "add";

    private boolean separateClass = true;
    private boolean mutableByDefault = false;

    CompilationUnit productUnit;
    private CompilationUnit builderUnit;

    ClassOrInterfaceDeclaration builderclass;
    private ClassOrInterfaceDeclaration productclass;

    private AdderHelper adderHelper = new AdderHelper(this);
    private MutatorHelper mutatorHelper = new MutatorHelper(this);
    private Naming naming = Naming.DEFAULT;

    /**
     * Creates a generator for an internal builder class.
     * <p>
     * <ul>
     * <li>creates the builder class definition
     * </ul>
     *
     * @param productUnit
     *            unit with the product class definition
     * @param productClassName
     *            name of the product class
     */
    BuilderGenerator(CompilationUnit productUnit, String productClassName) {
        this.productclass = ClassUtil.findClass(productUnit, productClassName).orElse(null);
        this.productUnit = productUnit;
        this.builderUnit = productUnit;
        this.separateClass = false;

        ifNotThrow(productclass != null, GeneratorException::productClassNotFound);
        ifNotThrow(hasUsableConstructor(productclass), GeneratorException::noConstructorFound);

        createInternalBuilderClass();
    }

    /**
     * Creates a generator for an external builder class.
     * <p>
     * <ul>
     * <li>adds the package declaration
     * <li>creates the builder class definition
     * <li>copies the import declarations from the product class
     * </ul>
     * Imports from the plugin package are excluded.
     *
     * @param productUnit
     *            unit with the product class definition
     * @param productClassName
     *            name of the product class
     * @param builderUnit
     *            unit with the builder class
     */
    BuilderGenerator(CompilationUnit productUnit, String productClassName, CompilationUnit builderUnit) {
        this.productclass = ClassUtil.findClass(productUnit, productClassName).orElse(null);
        this.productUnit = productUnit;
        this.builderUnit = builderUnit;

        ifNotThrow(productclass != null, GeneratorException::productClassNotFound);
        ifNotThrow(hasUsableConstructor(productclass), GeneratorException::noConstructorFound);

        createPackageDeclaration();
        copyImports();
        createExternalBuilderClass();
    }

    BuilderGenerator mutableByDefault(boolean mutableByDefault) {
        this.mutableByDefault = mutableByDefault;
        return this;
    }

    BuilderGenerator withNamingSettings(Naming naming) {
        this.naming = naming;
        return this;
    }

    private void createPackageDeclaration() {
        if (!builderUnit.getPackageDeclaration().isPresent()) {
            productUnit.getPackageDeclaration().ifPresent(builderUnit::setPackageDeclaration);
        }
    }

    private void copyImports() {
        productUnit.getImports().stream() //
                .filter(not(this::isBuilderImport)) //
                .forEach(builderUnit::addImport);
    }

    private boolean isBuilderImport(ImportDeclaration impDec) {
        return impDec.getNameAsString().startsWith(BUILDER_PACKAGE);
    }

    private void createExternalBuilderClass() {
        this.builderclass = builderUnit.findAll(ClassOrInterfaceDeclaration.class).stream() //
                .filter(c -> c.getNameAsString().equals(productClassName() + CLASS_POSTFIX)) //
                .filter(not(ClassOrInterfaceDeclaration::isInterface)) //
                .findFirst() //
                .orElseGet(() -> builderUnit.addClass(productClassName() + CLASS_POSTFIX));
    }

    private void createInternalBuilderClass() {
        this.builderclass = allMember(productclass, ClassOrInterfaceDeclaration.class) //
                .filter(c -> c.getNameAsString().equals(CLASS_POSTFIX)) //
                .findFirst() //
                .orElseGet(this::newInternalBuilderClass);
    }

    private ClassOrInterfaceDeclaration newInternalBuilderClass() {
        ClassOrInterfaceDeclaration memberClass = GenerationUtil.newMemberClass(CLASS_POSTFIX);
        productclass.getMembers().add(memberClass);
        return memberClass;
    }

    /**
     * Adds a field for the product to the builder.
     *
     * @return the generator instance
     */
    BuilderGenerator addProductField() {
        if (!hasProductField()) {
            builderclass.addField(productClassName(), FIELD, Modifier.Keyword.PRIVATE);
        }
        return this;
    }

    private boolean hasProductField() {
        Optional<VariableDeclarator> productField = findProductField();
        productField.filter(vd -> !vd.getType().equals(productClassType())).ifPresent(type -> {
            throw GeneratorException.productFieldHasWrongType(type);
        });
        return productField.isPresent();
    }

    Optional<VariableDeclarator> findProductField() {
        return allMember(builderclass, FieldDeclaration.class) //
                .map(FieldDeclaration::getVariables) //
                .flatMap(List::stream) //
                .filter(vd -> vd.getNameAsString().equals(FIELD)) //
                .findFirst();
    }

    /**
     * Adds a builder constructor for each constructor in the product class.
     *
     * @return the generator instance
     */
    BuilderGenerator addConstructors() {
        if (!hasExplicitConstructor(productclass) && !hasDefaultConstructor(builderclass)) {
            addDefaultConstructor();
        }
        allMember(productclass, ConstructorDeclaration.class) //
                .filter(this::process) //
                .filter(not(this::hasMatchingConstructor)) //
                .forEach(this::addMatchingConstructor);
        if (isProductMutable() && !hasManipulationConstructor()) {
            addManipulationConstructor();
        }
        return this;
    }

    private boolean hasManipulationConstructor() {
        return exists(//
                allMember(builderclass, ConstructorDeclaration.class) //
                        .filter(ClassUtil.hasSingleParameter(productClassType())));
    }

    private void addManipulationConstructor() {
        builderclass.addConstructor(Modifier.Keyword.PUBLIC) //
                .addParameter(productClassType(), FIELD) //
                .createBody() //
                .addStatement(assignExpr(fieldAccess(thisExpr(), FIELD), nameExpr(FIELD)));
    }

    private boolean process(ConstructorDeclaration cd) {
        if (cd.isAnnotationPresent(Ignore.class)) {
            return false;
        }
        if (cd.isPrivate() && separateClass) {
            return false;
        }
        return true;
    }

    private void addDefaultConstructor() {
        builderclass.addConstructor(Modifier.Keyword.PUBLIC) //
                .createBody() //
                .addStatement(assignExpr(FIELD, newExpr(productClassType())));
    }

    private void addMatchingConstructor(ConstructorDeclaration productConstructor) {
        ConstructorDeclaration builderconstructor = builderclass.addConstructor(Modifier.Keyword.PUBLIC);
        productConstructor.getParameters().stream().forEach(builderconstructor::addParameter);
        builderconstructor.createBody() //
                .addStatement(assignExpr(FIELD, newExpr(productClassType(), args(productConstructor))));
    }

    private boolean hasMatchingConstructor(ConstructorDeclaration productConstructor) {
        return allMember(builderclass, ConstructorDeclaration.class) //
                .anyMatch(cd -> ClassUtil.matchesParameter(cd, productConstructor));
    }

    /**
     * Adds a builder factory method for each product constructor.
     *
     * @return the generator instance
     */
    BuilderGenerator addFactoryMethods() {
        if (!hasProductConstructor(builderclass, productClassName())) {
            addProductConstructor();
        }
        if (!hasExplicitConstructor(productclass) && !hasDefaultFactoryMethod()) {
            addDefaultFactoryMethod();
        }
        if (isProductMutable() && !hasManipulationFactoryMethod()) {
            addManipulationFactoryMethod();
        }
        allMember(productclass, ConstructorDeclaration.class) //
                .filter(this::process) //
                .filter(not(this::hasMatchingFactoryMethod)) //
                .forEach(this::addFactoryMethod);

        return this;
    }

    private boolean hasManipulationFactoryMethod() {
        return exists(//
                allMember(builderclass, MethodDeclaration.class) //
                        .filter(MethodDeclaration::isStatic) //
                        .filter(md -> md.getNameAsString().equals(naming.factoryMethod())) //
                        .filter(md -> md.getTypeAsString().equals(builderClassName())) //
                        .filter(ClassUtil.hasSingleParameter(productClassType())));
    }

    private void addManipulationFactoryMethod() {
        MethodDeclaration factoryMethod = //
                builderclass.addMethod(naming.factoryMethod(), Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        factoryMethod.setType(builderClassName());
        factoryMethod.addParameter(productClassName(), FIELD);
        factoryMethod.createBody() //
                .addStatement(returnStmt(newExpr(builderClassType(), nameExpr(FIELD))));
    }

    /**
     * Adds a default factory method to the builder class.
     * <p>
     * signature: {@code public static Builder of();}
     */
    private void addDefaultFactoryMethod() {
        MethodDeclaration factoryMethod = //
                builderclass.addMethod(naming.factoryMethod(), Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        factoryMethod.setType(builderClassName());
        factoryMethod.createBody() //
                .addStatement(returnStmt(newExpr(builderClassType(), newExpr(productClassType()))));
    }

    private boolean hasDefaultFactoryMethod() {
        return exists(//
                allMember(builderclass, MethodDeclaration.class) //
                        .filter(MethodDeclaration::isStatic) //
                        .filter(md -> md.getNameAsString().equals(naming.factoryMethod())) //
                        .filter(md -> md.getTypeAsString().equals(builderClassName())) //
                        .filter(md -> md.getParameters().isEmpty()));
    }

    /**
     * Adds a product constructor to the builder.
     * <p>
     * signature {@code private Builder(Product product)}
     */
    void addProductConstructor() {
        ConstructorDeclaration constructor = builderclass.addConstructor(Modifier.Keyword.PRIVATE);
        constructor.addParameter(productClassName(), FIELD);
        constructor.createBody() //
                .addStatement(assignExpr(fieldAccess(thisExpr(), FIELD), nameExpr(FIELD)));
    }

    private void addFactoryMethod(ConstructorDeclaration productConstructor) {
        MethodDeclaration factoryMethod = //
                builderclass.addMethod(naming.factoryMethod(), Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        productConstructor.getParameters().stream().forEach(factoryMethod::addParameter);
        factoryMethod.setType(builderClassName());
        factoryMethod.createBody() //
                .addStatement(//
                        returnStmt(newExpr(builderClassType(), newExpr(productClassType(), args(productConstructor)))));
    }

    private boolean hasMatchingFactoryMethod(ConstructorDeclaration productConstructor) {
        return exists(//
                allMember(builderclass, MethodDeclaration.class) //
                        .filter(MethodDeclaration::isStatic) //
                        .filter(md -> md.getNameAsString().equals(naming.factoryMethod())) //
                        .filter(md -> md.getTypeAsString().equals(builderClassName())) //
                        .filter(md -> ClassUtil.matchesParameter(md, productConstructor)));
    }

    /**
     * Adds a mutator for each field in the product.
     *
     * @return the generator instance
     */
    BuilderGenerator addMutator(ListMutatorVariant[] mutatorVariants) {
        allMember(productclass, FieldDeclaration.class) //
                .filter(this::process) //
                .flatMap(fd -> mutatorDescriptors(mutatorVariants, fd)) //
                .filter(not(mutatorHelper::hasMutator)) //
                .forEach(mutatorHelper::addMutator);
        return this;
    }

    private Stream<MutatorMethodDescriptor> mutatorDescriptors(ListMutatorVariant[] mutatorVariants,
            FieldDeclaration fd) {
        return ClassUtil.isCollection(fd, productUnit) //
                ? new ListMutatorDescriptorGenerator(fd, mutatorVariants, naming.mutatorPrefix()).stream()
                : new MutatorDescriptorGenerator(fd, naming.mutatorPrefix()).stream();
    }

    private boolean process(FieldDeclaration fd) {
        if (fd.isAnnotationPresent(Ignore.class)) {
            return false;
        }
        if (fd.isPrivate() && separateClass) {
            return false;
        }
        return true;
    }

    /**
     * Adds the build method to the builder class.
     *
     * @return the generator instance
     */
    BuilderGenerator addBuildMethod() {
        if (!hasBuildMethod()) {
            builderclass.addMethod(naming.buildMethod(), Modifier.Keyword.PUBLIC) //
                    .setType(productClassType()) //
                    .createBody() //
                    .addStatement(returnStmt(nameExpr(FIELD)));
        }
        return this;
    }

    private boolean hasBuildMethod() {
        return exists(//
                allMember(builderclass, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(naming.buildMethod())) //
                        .filter(md -> md.getType().equals(productClassType())));
    }

    /**
     * Adds an adder method for each list implementing field in the product.
     *
     * @param adderVariants
     */
    BuilderGenerator addAdder(ListMutatorVariant[] adderVariants) {
        allMember(productclass, FieldDeclaration.class) //
                .filter(this::process) //
                .flatMap(fd -> new AdderMethodDescriptor.Generator(fd, adderVariants, productUnit).stream()) //
                .filter(not(adderHelper::hasAdder)) //
                .forEach(adderHelper::addAdder);
        return this;
    }

    ClassOrInterfaceType builderClassType() {
        return new ClassOrInterfaceType(builderClassName());
    }

    private String productClassName() {
        return productclass.getNameAsString();
    }

    private ClassOrInterfaceType productClassType() {
        return new ClassOrInterfaceType(productClassName());
    }

    private String builderClassName() {
        return builderclass.getNameAsString();
    }

    public ClassOrInterfaceDeclaration builderClass() {
        return builderclass;
    }

    /**
     * Returns the builder class.
     *
     * @return compilation unit with the builder class
     */
    CompilationUnit builderUnit() {
        return separateClass ? builderUnit : productUnit;
    }

    /**
     * Checks a class declaration for a usable constructor.
     * <p>
     * The constructor must be callable by the builder
     *
     * @param type
     *            class to check
     * @return {@code true} if the class contains fitting constructor.
     */
    private boolean hasUsableConstructor(ClassOrInterfaceDeclaration type) {
        List<ConstructorDeclaration> constructorList = //
                allMember(type, ConstructorDeclaration.class).collect(Collectors.toList());

        return constructorList.isEmpty() || constructorList.stream().anyMatch(this::process);
    }

    /**
     * Checks if the product class should be considered mutable.
     *
     * @return {@code true} if the class should be mutable
     */
    boolean isProductMutable() {
        return productclass.isAnnotationPresent(Mutable.class)
                || (mutableByDefault && !productclass.isAnnotationPresent(Immutable.class));
    }

}
