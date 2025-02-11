package net.andrecarbajal.mine_control_cli.model.vanilla;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class VanillaVersionsResponse {
    private List<VanillaVersion> versions;

    @Setter
    @Getter
    public static class VanillaVersion {
        private String id;
        private String type;
        private String url;
    }
}
