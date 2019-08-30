/**
 * Provides annotations to control the builder plugin.
 * <p>
 * <b>@GenerateBuilder</b><br>
 * For every class that is annotated with this annotation a builder class will
 * be generated. The Builder class will be stored in a separate class file.
 * <p>
 * <b>@GenerateAccessors</b><br>
 * For every field in the product class an accesssor is created in the peoduct
 * class unless the field is annotated with @Ignore
 * <p>
 * <b>@GenerateMutator</b><br>
 * A mutator is generated for the annotated field unless the field is annotated
 * with @Ignore. May be used for customization of the mutator since mutator
 * generation is the default for every field.
 * <p>
 * <b>@Ignore</b><br>
 * Fields an constructors that are annotated with this annotation are ignored by
 * the builder.<br>
 * No "with" method will be generated for an annotated field.<br>
 * No builder constructor will be generated for annotated constructors.
 * <p>
 * <b>@Immutable</b><br>
 * The annotated class is considered immutable, no manipulation facility will be
 * generated.
 * <p>
 * <b>@Mutable</b><br>
 * The annotated class is considered mutable, a manipulation facility will be
 * generated.
 */
package io.github.mletkin.numerobis.annotation;
