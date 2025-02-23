package net.andrecarbajal.mine_control_cli.model.mojang;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MojangServerResponse {
    private Downloads downloads;

    @Setter
    @Getter
    public static class Downloads {
        private Server server;

        @Setter
        @Getter
        public static class Server {
            private String url;

        }
    }
}
