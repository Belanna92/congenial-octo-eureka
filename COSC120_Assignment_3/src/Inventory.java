import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Inventory {

    private final Set<Garment> allGarments = new HashSet<>();

    public void addGarment(Garment Garment){
        this.allGarments.add(Garment);
    }

    public Set<String> getAllBrands(){
        Set<String> allBrands = new HashSet<>();
        for(Garment tee: allGarments){
            allBrands.add((String) tee.getGarmentSpecs().getFilter(Filter.BRAND));
        }
        allBrands.add("NA");
        return allBrands;
    }

    public List<Garment> findMatch(GarmentSpecs dreamGarment){
        List<Garment> matchingGarments = new ArrayList<>();
        for(Garment Garment: allGarments){
            if(!dreamGarment.matches(Garment.getGarmentSpecs())) continue;
            if(Garment.getPrice()<dreamGarment.getMinPrice()||Garment.getPrice()>dreamGarment.getMaxPrice()) continue;
            matchingGarments.add(Garment);
        }
        return matchingGarments;
    }

}
