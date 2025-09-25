package net.andrecarbajal.mine_control_cli.service.server.base;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ExecutionService;
import net.andrecarbajal.mine_control_cli.service.server.FabricServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.ForgeServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.MojangServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.NeoForgeServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.PaperServerCreator;

public class ServerCreatorFactory {
    public static IServerCreator getCreator(
            LoaderType loaderType,
            ConfigurationManager configurationManager,
            DownloadService downloadService,
            ExecutionService executionService) {
        return switch (loaderType) {
            case VANILLA, SNAPSHOT ->
                    new MojangServerCreator(loaderType, configurationManager, downloadService, executionService);
            case FABRIC -> new FabricServerCreator(configurationManager, downloadService, executionService);
            case PAPER -> new PaperServerCreator(configurationManager, downloadService, executionService);
            case NEOFORGE -> new NeoForgeServerCreator(configurationManager, downloadService, executionService);
            case FORGE -> new ForgeServerCreator(configurationManager, downloadService, executionService);
        };
    }
}
