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
package io.github.mletkin.numerobis.generator.mutator;

import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.generator.ListMutatorVariant;

/**
 * Descriptor for the generation of a mutator for a field.
 */
public class MutatorMethodDescriptor {

    private String methodName;
    private String parameterName;
    private Type parameterType; // actually the field type
    private ListMutatorVariant variant;

    public String methodName() {
        return methodName;
    }

    public String parameterName() {
        return parameterName;
    }

    public Type parameterType() {
        return parameterType;
    }

    public ListMutatorVariant variant() {
        return variant;
    }

    public static class Builder {

        private MutatorMethodDescriptor product;

        public Builder() {
            product = new MutatorMethodDescriptor();
        }

        public Builder withMethodName(String methodName) {
            product.methodName = methodName;
            return this;
        }

        public Builder withParameterName(String parameterName) {
            product.parameterName = parameterName;
            return this;
        }

        public Builder withParameterType(Type parameterType) {
            product.parameterType = parameterType;
            return this;
        }

        public Builder withVariant(ListMutatorVariant variant) {
            product.variant = variant;
            return this;
        }

        public MutatorMethodDescriptor build() {
            return product;
        }
    }
}
