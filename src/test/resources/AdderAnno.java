import java.util.List;
public class AdderAnno {
    @GenerateAdder(listVariants = {Variant.ITEM, Variant.STREAM})
    @GenerateMutator(listVariants = {Variant.OBJECT, Variant.STREAM})
    List<String> products;
}
