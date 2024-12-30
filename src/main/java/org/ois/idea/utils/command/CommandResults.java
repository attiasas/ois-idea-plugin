package org.ois.idea.utils.command;

public class CommandResults {
    private String stdOut;
    private String stdErr;
    private int exitCode;

    public boolean isOk() {
        return this.exitCode == 0;
    }

    public String getStdOut() {
        return this.stdOut;
    }

    public String getStdErr() {
        return this.stdErr;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    public void setStdOut(String stdout) {
        this.stdOut = stdout;
    }

    public void setStdErr(String stderr) {
        this.stdErr = stderr;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }
}
