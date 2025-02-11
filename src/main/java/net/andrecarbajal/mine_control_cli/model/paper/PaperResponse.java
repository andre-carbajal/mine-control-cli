package net.andrecarbajal.mine_control_cli.model.paper;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PaperResponse {
    private List<String> versions;
}
