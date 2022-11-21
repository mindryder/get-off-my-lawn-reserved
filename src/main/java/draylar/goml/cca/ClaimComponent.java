package draylar.goml.cca;

import com.jamieswhiteshirt.rtree3i.RTreeMap;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;

public interface ClaimComponent extends ComponentV3 {
    RTreeMap<ClaimBox, Claim> getClaims();
    void add(Claim info);
    void remove(Claim info);
}
