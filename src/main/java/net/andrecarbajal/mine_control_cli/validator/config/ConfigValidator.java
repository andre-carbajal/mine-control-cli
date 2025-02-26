package net.andrecarbajal.mine_control_cli.validator.config;

import net.andrecarbajal.mine_control_cli.config.properties.ConfigProperties;
import net.andrecarbajal.mine_control_cli.validator.core.ValidationResult;

public class ConfigValidator {
    private final RamValidator ramValidator = new RamValidator();
    private final JavaPathValidator javaPathValidator = new JavaPathValidator();
    private final DirectoryValidator directoryValidator = new DirectoryValidator();

    public ValidationResult validate(ConfigProperties properties) {
        ValidationResult ramResult = ramValidator.validate(properties.getServerRam());
        ValidationResult javaPathResult = javaPathValidator.validate(properties.getJavaPath());
        ValidationResult instancesPathResult = directoryValidator.validate(properties.getInstancesPath());
        ValidationResult backupsPathResult = directoryValidator.validate(properties.getBackupsPath());

        ValidationResult result = ramResult;
        result = result.merge(javaPathResult);
        result = result.merge(instancesPathResult);
        result = result.merge(backupsPathResult);

        return result;
    }
}
