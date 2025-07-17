package net.andrecarbajal.mine_control_cli.service.server.factory;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ServerProcessService;
import net.andrecarbajal.mine_control_cli.service.server.base.ServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.fabric.FabricServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.forge.ForgeServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.mojang.SnapshotServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.mojang.VanillaServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.neoforge.NeoForgeServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.paper.PaperServerCreator;

public class ServerCreatorFactory {
    public static ServerCreator getCreator(
            LoaderType loaderType,
            ConfigurationManager configurationManager,
            DownloadService downloadService,
            ServerProcessService serverProcessService) {
        return switch (loaderType) {
            case VANILLA -> new VanillaServerCreator(loaderType, configurationManager, downloadService);
            case SNAPSHOT -> new SnapshotServerCreator(loaderType, configurationManager, downloadService);
            case FABRIC -> new FabricServerCreator(configurationManager, downloadService);
            case PAPER -> new PaperServerCreator(loaderType, configurationManager, downloadService);
            case NEOFORGE ->
                    new NeoForgeServerCreator(loaderType, configurationManager, downloadService, serverProcessService);
            case FORGE ->
                    new ForgeServerCreator(loaderType, configurationManager, downloadService, serverProcessService);
        };
    }
}
