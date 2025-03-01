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
package io.github.mletkin.numerobis.generator.common;

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
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.Type;

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
     *                      compilation unit to check
     * @param className
     *                      name of the searched class
     * @return the found class declaration wrapped in an {@link Optional}
     */
    public static Optional<ClassOrInterfaceDeclaration> findClass(CompilationUnit unit, String className) {
        return unit.findFirst(ClassOrInterfaceDeclaration.class, //
                cl -> cl.getNameAsString().equals(className));
    }

    /**
     * Checks a compilation unit for a given record.
     *
     * @param unit
     *                       compilation unit to check
     * @param recordName
     *                       name of the searched record
     * @return the found record declaration wrapped in an {@link Optional}
     */
    public static Optional<RecordDeclaration> findRecord(CompilationUnit unit, String recordName) {
        return unit.findFirst(RecordDeclaration.class, //
                cl -> cl.getNameAsString().equals(recordName));
    }

    /**
     * Checks a class declaration for a constructor.
     *
     * @param type
     *                 class to check
     * @return {@code true} if the class contains a constructor
     */
    public static boolean hasExplicitConstructor(ClassOrInterfaceDeclaration type) {
        return exists(allMember(type, ConstructorDeclaration.class));
    }

    /**
     * Checks a class declaration for a default constructor.
     *
     * @param type
     *                 class to check
     * @return {@code true} if the class contains a default constructor.
     */
    public static boolean hasDefaultConstructor(ClassOrInterfaceDeclaration type) {
        return exists(allMember(type, ConstructorDeclaration.class) //
                .filter(cd -> cd.getParameters().isEmpty()));
    }

    /**
     * Checks a class declaration for a product constructor.
     *
     * @param type
     *                             class to check
     * @param productClassName
     *                             name of the product class
     * @return {@code true} if the class contains a product constructor.
     */
    public static boolean hasProductConstructor(ClassOrInterfaceDeclaration type, String productClassName) {
        return allMember(type, ConstructorDeclaration.class) //
                .filter(cd -> cd.getParameters().size() == 1) //
                .anyMatch(cd -> cd.getParameter(0).getTypeAsString().equals(productClassName));
    }

    /**
     * Checks, whether a variable type is a {@code Collection}.
     *
     * @param vd
     *               declaration of the variable to check
     * @param cu
     *               Compilation unit with imports
     * @return {@code true}, if the type is a {@code Collection}
     */
    public static boolean isCollection(VariableDeclarator vd, CompilationUnit cu) {
        return extendsInterface(vd.getType(), Collection.class, cu);
    }

    /**
     * Checks, whether a field declaration type is a {@code Collection}.
     *
     * @param fd
     *               field declaration to check
     * @param cu
     *               Compilation unit with imports
     * @return {@code true}, if the type is a {@code Collection}
     */
    public static boolean isCollection(FieldDeclaration fd, CompilationUnit cu) {
        return extendsInterface(fd.getElementType(), Collection.class, cu);
    }

    /**
     * Checks, whether a type extends a given interface.
     *
     * @param type
     *                  Type to check
     * @param clazz
     *                  Class object of the interface
     * @param cu
     *                  Compilation unit with imports
     * @return {@code true}, if the type extends the interface
     */
    public static boolean extendsInterface(Type type, Class<?> clazz, CompilationUnit cu) {
        String typ = type.findFirst(SimpleName.class).map(SimpleName::asString).orElse("#");

        Optional<String> fullType = cu.getImports().stream() //
                .map(ImportDeclaration::getNameAsString) //
                .filter(i -> i.endsWith(typ)) //
                .findFirst();

        try {
            if (fullType.isPresent()) {
                Class<?> c = Class.forName(fullType.get());
                c.asSubclass(clazz);
                return true;
            }
        } catch (ClassCastException | ClassNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * Compares the parameter types of two method or constructor declarations.
     *
     * @param a
     *              first declaration to compare
     * @param b
     *              second declaration to compare
     * @return {@code true} if both type lists are identical
     */
    public static boolean matchesParameter(CallableDeclaration<?> a, CallableDeclaration<?> b) {
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
     * Returns all members of a given type for a type declaration.
     *
     * @param <T>
     *                       member Type
     * @param decl
     *                       type declaration
     * @param memberType
     *                       class object of the member type
     * @return stream of members
     */
    public static <T extends Node> Stream<T> allMember(TypeDeclaration decl, Class<T> memberType) {
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
     *                 the type, the parameter must have
     * @return the predicate
     */
    public static Predicate<CallableDeclaration<?>> hasSingleParameter(Type type) {
        return md -> md.getParameters().size() == 1 && md.getParameter(0).getType().equals(type);
    }

    /**
     * Produces a predicate to check that a method has exactly one vararg parameter.
     *
     * @param type
     *                 the type, the parameter must have
     * @return the predicate
     */
    public static Predicate<CallableDeclaration<?>> hasSingleVarArgParameter(Type type) {
        return md -> md.getParameters().size() == 1 //
                && md.getParameter(0).getType().equals(type) //
                && md.getParameter(0).isVarArgs();
    }

    /**
     * Returns the type of the first type parameter.
     *
     * @param type
     *                 type with parameters
     * @return type of the first type parameter
     */
    public static Type firstTypeArgument(Type type) {
        return type.asClassOrInterfaceType().getTypeArguments().get().get(0);
    }
}
