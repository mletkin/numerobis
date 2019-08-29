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
import static io.github.mletkin.numerobis.generator.ClassUtil.allMember;
import static io.github.mletkin.numerobis.generator.ClassUtil.hasDefaultConstructor;
import static io.github.mletkin.numerobis.generator.ClassUtil.hasExplicitConstructor;
import static io.github.mletkin.numerobis.generator.ClassUtil.hasProductConstructor;
import static io.github.mletkin.numerobis.generator.GenerationUtil.args;
import static io.github.mletkin.numerobis.generator.GenerationUtil.assignExpr;
import static io.github.mletkin.numerobis.generator.GenerationUtil.fieldAccess;
import static io.github.mletkin.numerobis.generator.GenerationUtil.methodCall;
import static io.github.mletkin.numerobis.generator.GenerationUtil.nameExpr;
import static io.github.mletkin.numerobis.generator.GenerationUtil.newExpr;
import static io.github.mletkin.numerobis.generator.GenerationUtil.returnStmt;
import static io.github.mletkin.numerobis.generator.GenerationUtil.thisExpr;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.annotation.Ignore;
import io.github.mletkin.numerobis.annotation.Immutable;
import io.github.mletkin.numerobis.annotation.Mutable;

/**
 * Generates builder classes product classes in a seperate compilation unit.
 */
public class BuilderGenerator {

    private static final String BUILDER_PACKAGE = "io.github.mletkin.numerobis";
    private final static String FIELD = "product";
    private final static String CLASS_POSTFIX = "Builder";

    final static String BUILD_METHOD = "build";
    final static String FACTORY_METHOD = "of";
    final static String MUTATOR_PREFIX = "with";
    final static String ADDER_PREFIX = "add";

    private boolean separateClass = true;
    private boolean mutableByDefault = false;

    private CompilationUnit productUnit;
    private CompilationUnit builderUnit;

    private ClassOrInterfaceDeclaration builderclass;
    private ClassOrInterfaceDeclaration productclass;

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
        addDefaultConstructorIfNeeded();
        allMember(productclass, ConstructorDeclaration.class) //
                .filter(this::process) //
                .forEach(this::addMatchingConstructor);
        addManipulationConstructorIfNeeded();
        return this;
    }

    private void addManipulationConstructorIfNeeded() {
        if (mutable(productclass) && !hasManipulationConstructor()) {
            addManipulationConstructor();
        }
    }

    private boolean hasManipulationConstructor() {
        return exists(//
                allMember(builderclass, ConstructorDeclaration.class) //
                        .filter(hasSingleParameterOfType(productClassType())));
    }

    private Predicate<ConstructorDeclaration> hasSingleParameterOfType(Type type) {
        return md -> md.getParameters().size() == 1 && md.getParameter(0).getType().equals(type);
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

    private void addDefaultConstructorIfNeeded() {
        if (!hasExplicitConstructor(productclass) && !hasDefaultConstructor(builderclass)) {
            builderclass.addConstructor(Modifier.Keyword.PUBLIC) //
                    .createBody() //
                    .addStatement(assignExpr(FIELD, newExpr(productClassType())));
        }
    }

    private void addMatchingConstructor(ConstructorDeclaration productConstructor) {
        if (!hasMatchingConstructor(productConstructor)) {
            ConstructorDeclaration builderconstructor = builderclass.addConstructor(Modifier.Keyword.PUBLIC);
            productConstructor.getParameters().stream().forEach(builderconstructor::addParameter);
            builderconstructor.createBody() //
                    .addStatement(assignExpr(FIELD, newExpr(productClassType(), args(productConstructor))));
        }
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
        if (mutable(productclass)) {
            addManipulationFactoryMethod();
        }
        allMember(productclass, ConstructorDeclaration.class) //
                .filter(this::process) //
                .filter(not(this::hasMatchingFactoryMethod)) //
                .forEach(this::addFactoryMethod);

        return this;
    }

    private void addManipulationFactoryMethod() {
        MethodDeclaration factoryMethod = //
                builderclass.addMethod(FACTORY_METHOD, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
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
                builderclass.addMethod(FACTORY_METHOD, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        factoryMethod.setType(builderClassName());
        factoryMethod.createBody() //
                .addStatement(returnStmt(newExpr(builderClassType(), newExpr(productClassType()))));
    }

    private boolean hasDefaultFactoryMethod() {
        return exists(//
                allMember(builderclass, MethodDeclaration.class) //
                        .filter(MethodDeclaration::isStatic) //
                        .filter(md -> md.getNameAsString().equals(FACTORY_METHOD)) //
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
                builderclass.addMethod(FACTORY_METHOD, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
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
                        .filter(md -> md.getNameAsString().equals(FACTORY_METHOD)) //
                        .filter(md -> md.getTypeAsString().equals(builderClassName())) //
                        .filter(md -> ClassUtil.matchesParameter(md, productConstructor)));
    }

    /**
     * Adds a mutator for each field in the product.
     *
     * @return the generator instance
     */
    BuilderGenerator addMutator() {
        allMember(productclass, FieldDeclaration.class) //
                .filter(this::process) //
                .flatMap(fd -> new MutatorMethodDescriptor.Generator(fd).stream()) //
                .filter(not(this::hasMutator)) //
                .forEach(this::addMutator);
        return this;
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

    private void addMutator(MutatorMethodDescriptor mmd) {
        MethodDeclaration meth = builderclass.addMethod(mmd.methodName, Modifier.Keyword.PUBLIC);
        meth.addParameter(mmd.parameterType, mmd.parameterName);
        meth.setType(builderClassType());
        meth.createBody() //
                .addStatement(assignExpr(fieldAccess(nameExpr(FIELD), mmd.parameterName), nameExpr(mmd.parameterName))) //
                .addStatement(returnStmt(thisExpr()));
    }

    private boolean hasMutator(MutatorMethodDescriptor mmd) {
        return exists(//
                allMember(builderclass, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(mmd.methodName)) //
                        .filter(hasSingleParameter(mmd.parameterType)) //
                        .filter(md -> md.getType().equals(builderClassType())));
    }

    private Predicate<MethodDeclaration> hasSingleParameter(Type type) {
        return md -> md.getParameters().size() == 1 && md.getParameter(0).getType().equals(type);
    }

    /**
     * Adds the build method to the builder class.
     *
     * @return the generator instance
     */
    BuilderGenerator addBuildMethod() {
        if (!hasBuildMethod()) {
            builderclass.addMethod(BUILD_METHOD, Modifier.Keyword.PUBLIC) //
                    .setType(productClassType()) //
                    .createBody() //
                    .addStatement(returnStmt(nameExpr(FIELD)));
        }
        return this;
    }

    private boolean hasBuildMethod() {
        return exists(//
                allMember(builderclass, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(BUILD_METHOD)) //
                        .filter(md -> md.getType().equals(productClassType())));
    }

    /**
     * Adds an adder method for each list implementing field in the product.
     */
    BuilderGenerator addAdderMethods() {
        allMember(productclass, FieldDeclaration.class) //
                .filter(this::process) //
                .flatMap(fd -> new AdderMethodDescriptor.Generator(fd, productUnit).stream()) //
                .filter(not(this::hasAdderMethod)) //
                .forEach(this::addAdderMethod);
        return this;
    }

    private void addAdderMethod(AdderMethodDescriptor amd) {
        MethodDeclaration meth = builderclass.addMethod(amd.methodName, Modifier.Keyword.PUBLIC);
        meth.addParameter(amd.parameterType, "item");
        meth.setType(builderClassType());
        meth.createBody() //
                .addStatement(methodCall(fieldAccess(nameExpr(FIELD), amd.fieldName), "add", nameExpr("item"))) //
                .addStatement(returnStmt(thisExpr()));
    }

    /**
     * Checks for an add method in the builder class.
     * <p>
     * signature {@code Builder addName(Type item)}
     *
     * @param amd
     *            adder descriptor
     * @return {@code true} if the method exists
     */
    private boolean hasAdderMethod(AdderMethodDescriptor amd) {
        return exists(//
                allMember(builderclass, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(amd.methodName)) //
                        .filter(hasSingleParameter(amd.parameterType)) //
                        .filter(md -> md.getType().equals(builderClassType())));
    }

    private ClassOrInterfaceType builderClassType() {
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
     * Checks if the (product) class should be considered mutable.
     *
     * @param decl
     *            class to check
     * @return {@code true} if the class should be mutable
     */
    boolean mutable(ClassOrInterfaceDeclaration decl) {
        return (!mutableByDefault && decl.isAnnotationPresent(Mutable.class))
                || (mutableByDefault && !decl.isAnnotationPresent(Immutable.class));
    }

}
