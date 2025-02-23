package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.springframework.stereotype.Service;

@Service
public class VanillaService extends MojangService {
    public VanillaService(FileUtil fileUtil, FileDownloadService fileDownloadService) {
        super(fileUtil, fileDownloadService);
    }

    @Override
    public String type() {
        return "release";
    }
}