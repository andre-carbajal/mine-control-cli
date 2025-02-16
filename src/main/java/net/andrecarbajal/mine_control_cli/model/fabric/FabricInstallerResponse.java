package net.andrecarbajal.mine_control_cli.model.fabric;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FabricInstallerResponse {
    private String version;
    private boolean stable;
}
