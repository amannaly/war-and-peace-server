package com.mannaly.arjun;

public enum OsType {
    OSX, LINUX, WINDOWS;

    public static OsType getOsType() {
        String osName = System.getProperty("os.name").toLowerCase();

        OsType osType = null;
        if (osName.contains("mac")) {
            osType = OsType.OSX;
        }
        else if (osName.contains("nix")) {
            osType = OsType.LINUX;
        }
        else if (osName.contains("win")) {
            osType = OsType.WINDOWS;
        }
        return osType;
    }
}
