/**
 * Provides annotations to control the builder plugin.
 * <p>
 * {@link @WithBuilder}<br>
 * For every class that is annotated with this annotation a builder class will
 * be generated. The Builder class will be stored in a separate class file.
 * <p>
 * {@link @Ignore}<br>
 * Fields an constructors that are annotated with this annotation are ignored by
 * the builder.<br>
 * No "with" method will be generated for an annotated field.<br>
 * No builder constructor will be generated for annotated constructors.
 */
package io.github.mletkin.numerobis.annotation;
