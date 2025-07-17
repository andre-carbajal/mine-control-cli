package net.andrecarbajal.mine_control_cli.model;

import lombok.Getter;

public enum LoaderType {
    VANILLA("Vanilla"),
    SNAPSHOT("Snapshot"),
    FABRIC("Fabric"),
    PAPER("Paper"),
    NEOFORGE("NeoForge"),
    FORGE("Forge");

    @Getter
    private final String displayName;

    LoaderType(String displayName) {
        this.displayName = displayName;
    }
}