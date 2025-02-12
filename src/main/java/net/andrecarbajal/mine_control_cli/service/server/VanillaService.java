package net.andrecarbajal.mine_control_cli.service.server;

import org.springframework.stereotype.Service;

@Service
public class VanillaService extends MojangService {
    @Override
    public String type() {
        return "release";
    }
}