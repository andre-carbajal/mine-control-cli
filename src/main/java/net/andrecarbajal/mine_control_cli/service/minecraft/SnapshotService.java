package net.andrecarbajal.mine_control_cli.service.minecraft;

import org.springframework.stereotype.Service;

@Service
public class SnapshotService extends MojangService{
    @Override
    public String type() {
        return "snapshot";
    }
}
