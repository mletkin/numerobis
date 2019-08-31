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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.annotation.Ignore;

/**
 * Convenience Methods for class declarations.
 */
public final class ClassUtil {

    private ClassUtil() {
        // prevent instantiation
    }

    /**
     * Checks a compilation unit for a given class.
     *
     * @param unit
     *            compilation unit to check
     * @param className
     *            name of the searched class
     * @return the found class declaration wrapped in an optional
     */
    public static Optional<ClassOrInterfaceDeclaration> findClass(CompilationUnit unit, String className) {
        return unit.findFirst(ClassOrInterfaceDeclaration.class, //
                cl -> cl.getNameAsString().equals(className));
    }

    /**
     * Checks a class declaration for a constructor.
     *
     * @param type
     *            class to check
     * @return {@code true} if the class contains a constructor
     */
    static boolean hasExplicitConstructor(ClassOrInterfaceDeclaration type) {
        return exists(allMember(type, ConstructorDeclaration.class));
    }

    /**
     * Checks a class declaration for a default constructor.
     *
     * @param type
     *            class to check
     * @return {@code true} if the class contains a default constructor.
     */
    static boolean hasDefaultConstructor(ClassOrInterfaceDeclaration type) {
        return exists(allMember(type, ConstructorDeclaration.class) //
                .filter(cd -> cd.getParameters().isEmpty()));
    }

    /**
     * Checks a class declaration for a product constructor.
     *
     * @param type
     *            class to check
     * @return {@code true} if the class contains a product constructor.
     */
    static boolean hasProductConstructor(ClassOrInterfaceDeclaration type, String productClassName) {
        return allMember(type, ConstructorDeclaration.class) //
                .filter(cd -> cd.getParameters().size() == 1) //
                .anyMatch(cd -> cd.getParameter(0).getTypeAsString().equals(productClassName));
    }

    /**
     * Checks a constructor declaration for builder usage.
     *
     * @param cd
     *            constructor declartion to check
     * @return {@code true} if the builder should have a corresponding constructor
     */
    static boolean process(ConstructorDeclaration cd) {
        return !cd.isPrivate() && !cd.isAnnotationPresent(Ignore.class);
    }

    static boolean implementsCollection(VariableDeclarator vd, CompilationUnit cu) {
        String typ = vd.getType().findFirst(SimpleName.class).map(SimpleName::asString).orElse("#");

        Optional<String> fullType = cu.getImports().stream() //
                .map(ImportDeclaration::getNameAsString) //
                .filter(i -> i.endsWith(typ)) //
                .findFirst();

        try {
            if (fullType.isPresent()) {
                Class<?> c = Class.forName(fullType.get());
                c.asSubclass(Collection.class);
                return true;
            }
        } catch (ClassCastException | ClassNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * Compares the types of the parameter Lists of two method or coantructor
     * declarations.
     *
     * @param a
     *            first declaration to compare
     * @param b
     *            second declaration to compare
     * @return {@code true} if both type lists are identicalx
     */
    static boolean matchesParameter(CallableDeclaration<?> a, CallableDeclaration<?> b) {
        if (a.getParameters().size() != b.getParameters().size()) {
            return false;
        }
        for (int n = 0; n < a.getParameters().size(); n++) {
            if (!a.getParameter(n).getTypeAsString().equals(b.getParameter(n).getTypeAsString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets all members of a given type for a class declaration.
     *
     * @param <T>
     *            member Type
     * @param decl
     *            class declaration
     * @param memberType
     *            class object of the member type
     * @return stream of members
     */
    public static <T extends Node> Stream<T> allMember(ClassOrInterfaceDeclaration decl, Class<T> memberType) {
        return decl.findAll(memberType) //
                .stream() //
                .filter(isMember(decl));
    }

    private static Predicate<Node> isMember(Node parent) {
        return node -> node.getParentNode().orElse(null) == parent;
    }

    /**
     * Produces a predicate to check that a method has exactly one parameter.
     *
     * @param type
     *            the type, the parameter must have
     * @return the predicate
     */
    static Predicate<CallableDeclaration<?>> hasSingleParameter(Type type) {
        return md -> md.getParameters().size() == 1 && md.getParameter(0).getType().equals(type);
    }

    /**
     * Produces a predicate to check that a method has exactly one vararg parameter.
     *
     * @param type
     *            the type, the parameter must have
     * @return the predicate
     */
    static Predicate<CallableDeclaration<?>> hasSingleVarArgParameter(Type type) {
        return md -> md.getParameters().size() == 1 //
                && md.getParameter(0).getType().equals(type) //
                && md.getParameter(0).isVarArgs();
    }

}
