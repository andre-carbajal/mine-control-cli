package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.MineControlConfig;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.springframework.stereotype.Service;

@Service
public class VanillaService extends MojangService {
    public VanillaService(MineControlConfig mineControlConfig, FileUtil fileUtil, FileDownloadService fileDownloadService) {
        super(mineControlConfig, fileUtil, fileDownloadService);
    }

    @Override
    public String type() {
        return "release";
    }
}