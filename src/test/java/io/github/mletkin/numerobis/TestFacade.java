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
package io.github.mletkin.numerobis;

import static io.github.mletkin.numerobis.Util.asString;
import static io.github.mletkin.numerobis.Util.extractBuilder;
import static io.github.mletkin.numerobis.Util.uncheckExceptions;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;

/**
 * Utilities for unit tests.
 */
public class TestFacade {

    private Facade facade;

    TestFacade(boolean mutableByDefault) {
        facade = new Facade(mutableByDefault);
    }

    String externalWithConstructors(String className) {
        return uncheckExceptions(() -> //
        asString(facade.withConstructors(StaticJavaParser.parseResource(className + ".java"), className,
                new CompilationUnit()).builderUnit));
    }

    String externalWithConstructors(String className, String builderClass) {
        return uncheckExceptions(() -> {
            CompilationUnit source = StaticJavaParser.parseResource(className + ".java");
            CompilationUnit target = StaticJavaParser.parse(builderClass);

            return asString(facade.withConstructors(source, className, target).builderUnit);
        });
    }

    String internalWithConstructors(String className) {
        return uncheckExceptions(() -> asString(extractBuilder(//
                facade.withConstructors(StaticJavaParser.parseResource(className + ".java"), className) //
                        .productUnit,
                className)));
    }

    String externalWithFactories(String className) {
        return uncheckExceptions(
                () -> asString(facade.withFactoryMethods(StaticJavaParser.parseResource(className + ".java"), className,
                        new CompilationUnit()).builderUnit));
    }

    String externalWithFactories(String className, String builderClass) {
        return uncheckExceptions(() -> {
            CompilationUnit source = StaticJavaParser.parseResource(className + ".java");
            CompilationUnit target = StaticJavaParser.parse(builderClass);

            return asString(facade.withFactoryMethods(source, className, target).builderUnit);
        });
    }

    String internalWithFactories(String className) {
        return uncheckExceptions(() -> asString(extractBuilder(
                facade.withFactoryMethods(StaticJavaParser.parseResource(className + ".java"), className).productUnit,
                className)));
    }

}
