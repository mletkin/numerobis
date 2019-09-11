package io.github.mletkin.numerobis.generator.mutator;

import java.util.Optional;
import java.util.stream.Stream;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import io.github.mletkin.numerobis.annotation.GenerateListMutator;
import io.github.mletkin.numerobis.common.Util;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;
import io.github.mletkin.numerobis.generator.common.StringExtractor;
import io.github.mletkin.numerobis.generator.common.VariantExtractor;

/**
 * Generator for lust field mutator descriptor objects.
 * <p>
 * NB: One field declaration can contain more than one variable ( e.g.
 * {@code int x,y;})
 *
 * TODO handle List&lt;T&gt;[] correctly
 */
public class ListMutatorDescriptorGenerator {
    private static ListMutatorVariant[] DEFAULT = { ListMutatorVariant.OBJECT };

    private FieldDeclaration field;
    private ListMutatorVariant[] variants;
    private String mutatorPrefix;

    public ListMutatorDescriptorGenerator(FieldDeclaration field, ListMutatorVariant[] variants, String mutatoPrefix) {
        this.field = field;
        this.variants = Util.firstNotEmpty( //
                new VariantExtractor(GenerateListMutator.class).variants(field), //
                variants) //
                .orElse(DEFAULT);
        this.mutatorPrefix = mutatoPrefix;
    }

    /**
     * Returns a stream of method descriptors from a field declaration.
     *
     * @return a stream of method descriptors
     */
    public Stream<MutatorMethodDescriptor> stream() {
        return field.getVariables().stream() //
                .flatMap(this::toVariants);
    }

    private Stream<MutatorMethodDescriptor> toVariants(VariableDeclarator vd) {
        return Stream.of(variants) //
                .filter(v -> v != ListMutatorVariant.NONE) //
                .map(v -> map(vd, v));
    }

    private MutatorMethodDescriptor map(VariableDeclarator vd, ListMutatorVariant variant) {
        return new MutatorMethodDescriptor.Builder() //
                .withMethodName(methodName(vd)) //
                .withParameterName(vd.getNameAsString()) //
                .withParameterType(vd.getType()) //
                .withVariant(variant) //
                .build();
    }

    private String methodName(VariableDeclarator vd) {
        return customName().orElseGet(() -> standardMutatorName(vd));
    }

    private String standardMutatorName(VariableDeclarator vd) {
        return mutatorPrefix + Util.firstLetterUppercase(vd.getNameAsString());
    }

    private Optional<String> customName() {
        return new StringExtractor(GenerateListMutator.class, "name").value(field);
    }

}
