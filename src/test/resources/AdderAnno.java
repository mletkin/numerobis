import java.util.List;
public class AdderAnno {
    @GenerateAdder(variants = {Variant.ITEM, Variant.STREAM})
    @GenerateListMutator(variants = {Variant.OBJECT, Variant.STREAM})
    List<String> products;
}
