import java.util.List;
public class WithListWithCustomName {
    @GenerateListMutator(name="foo")
    List<String> x = new ArrayList<>();
}
