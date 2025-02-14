package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import org.springframework.stereotype.Service;

@Service
public class VanillaService extends MojangService {
    public VanillaService(FileDownloadService fileDownloadService) {
        super(fileDownloadService);
    }

    @Override
    public String type() {
        return "release";
    }
}