package net.andrecarbajal.mine_control_cli.service.server.forge;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ExecutionService;
import net.andrecarbajal.mine_control_cli.service.server.base.AbstractForgeBasedModdedServerCreator;
import net.andrecarbajal.mine_control_cli.util.ApiClientUtil;
import net.andrecarbajal.mine_control_cli.util.VersionComparator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ForgeServerCreator extends AbstractForgeBasedModdedServerCreator {

    public ForgeServerCreator(LoaderType loaderType, ConfigurationManager configurationManager, DownloadService downloadService, ExecutionService executionService) {
        super(loaderType, configurationManager, downloadService, executionService);
    }

    @Override
    protected String getApiUrl() {
        return "https://bmclapi2.bangbang93.com/forge/";
    }

    @Override
    protected List<String> getVersions() {
        List<String> versionsList = new ArrayList<>();
        JSONArray jsonArray = ApiClientUtil.getJsonArray(getApiUrl() + "minecraft");
        for (int i = 0; i < jsonArray.length(); i++) {
            String version = jsonArray.getString(i);
            versionsList.add(version);
        }
        versionsList.sort(new VersionComparator());
        return versionsList;
    }

    @Override
    protected List<String> getLoaderVersions(String minecraftVersion) {
        List<String> loaderVersionsList = new ArrayList<>();
        JSONArray jsonArray = ApiClientUtil.getJsonArray(getApiUrl() + "minecraft/" + minecraftVersion);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject loaderObject = jsonArray.getJSONObject(i);
            String version = loaderObject.getString("version");
            loaderVersionsList.add(version);
        }
        return loaderVersionsList.reversed();
    }

    @Override
    protected String getDownloadUrl(String version, String loaderVersion) {
        String forgeVersion = version + "-" + loaderVersion;
        return "https://maven.minecraftforge.net/net/minecraftforge/forge/" + forgeVersion + "/forge-" + forgeVersion + "-installer.jar";
    }
}
