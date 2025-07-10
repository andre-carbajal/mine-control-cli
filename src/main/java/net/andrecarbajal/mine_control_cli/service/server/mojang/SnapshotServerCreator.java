package net.andrecarbajal.mine_control_cli.service.server.mojang;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;

public class SnapshotServerCreator extends MojangServerCreator {
    public SnapshotServerCreator(LoaderType loaderType, ConfigurationManager configurationManager, DownloadService downloadService) {
        super(loaderType, configurationManager, downloadService);
    }

    @Override
    protected String getVersionType() {
        return "snapshot";
    }
}
