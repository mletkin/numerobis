import java.util.List;
public class AdderAnnoNone {
    @GenerateAdder(listVariants = {Variant.NONE})
    List<String> products;
}
