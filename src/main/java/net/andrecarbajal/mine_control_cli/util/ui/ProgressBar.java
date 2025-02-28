package net.andrecarbajal.mine_control_cli.util.ui;

import lombok.Getter;
import lombok.Setter;
import org.jline.terminal.Terminal;

public class ProgressBar {
    private static final String CUU = "\u001B[A";
    private static final String DL = "\u001B[1M";

    @Setter
    @Getter
    private String doneMarker = "=";
    @Setter
    @Getter
    private String remainsMarker = "-";
    @Setter
    @Getter
    private String leftDelimiter = "<";
    @Setter
    @Getter
    private String rightDelimiter = ">";

    Terminal terminal;

    private boolean started = false;
    private int lastPercentage = 0;

    public ProgressBar(Terminal terminal) {
        this.terminal = terminal;
    }

    public void display(int percentage) {
        display(percentage, null);
    }

    public void display(int percentage, String statusMessage) {
        if (!started) {
            started = true;
            terminal.writer().println();
        }
        if (percentage == lastPercentage) {
            return;
        }
        int x = (percentage / 5);
        int y = 20 - x;
        String message = ((statusMessage == null) ? "" : statusMessage);

        String done = new String(new char[x]).replace("\0", doneMarker);
        String remains = new String(new char[y]).replace("\0", remainsMarker);

        String progressBar = String.format("%s%s%s%s %d", leftDelimiter, done, remains, rightDelimiter, percentage);

        terminal.writer().println(CUU + "\r" + DL + progressBar + "% " + message);
        terminal.flush();
    }

    public void reset() {
        lastPercentage = 0;
        started = false;
    }
}
