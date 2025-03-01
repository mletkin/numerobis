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

import com.github.javaparser.ast.body.VariableDeclarator;

/**
 * Exception thrown by the builder generator.
 * <p>
 * Only one exception class is defined. Different error cases have different messages.
 */
public class GeneratorException extends RuntimeException {

    private GeneratorException(String message) {
        super(message);
    }

    public static GeneratorException noConstructorFound() {
        return new GeneratorException("No suitable constructor found.");
    }

    public static GeneratorException productClassNotFound() {
        return new GeneratorException("Product class not found in compilation unit.");
    }

    public static GeneratorException productClassNotFound(String clazz) {
        var myClazz = (clazz == null || clazz.isBlank()) ? "" : clazz.trim() + " ";
        return new GeneratorException("Product class " + myClazz + "not found in compilation unit.");
    }

    public static GeneratorException productFieldHasWrongType(VariableDeclarator vd) {
        return new GeneratorException("The product field has the wrong type " + vd.getTypeAsString() + ".");
    }

}
