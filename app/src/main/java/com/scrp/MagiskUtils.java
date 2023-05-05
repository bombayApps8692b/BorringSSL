package com.scrp;

import java.io.File;

public class MagiskUtils {

    public boolean isDeviceRooted() {
        return isrooted1() || isrooted2();
    }

    public boolean isDeviceRootedByMagisk() {
        return isrooted1() || isrooted2() || checkForMagiskBinary() || checkForFridaBinary();
    }

    private static boolean isrooted2() {
        return canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su")
                || canExecuteCommand("which su");
    }

    private static boolean isrooted1() {
        File file = new File("/system/app/Superuser.apk");
        return file.exists();
    }

    private static boolean checkForMagiskBinary() {
        String[] pathsArray = suPaths;
        boolean result = false;
        for (String path : pathsArray) {
            File f = new File(path, "magisk");
            boolean fileExists = f.exists();
            if (fileExists) {
                result = true;
            }
        }
        return result;
    }

    private static boolean checkForFridaBinary() {
        String[] pathsArray = suPaths;
        boolean result = false;
        for (String path : pathsArray) {
            File f = new File(path, "frida-server-32");
            boolean fileExists = f.exists();
            if (fileExists) {
                result = true;
            }
        }
        return result;
    }

    private static boolean canExecuteCommand(String s) {
        return false;
    }

    private static final String[] suPaths = {
            "/data/local/",
            "/data/local/bin/",
            "/data/local/xbin/",
            "/sbin/",
            "/su/bin/",
            "/system/bin/",
            "/system/bin/.ext/",
            "/system/bin/failsafe/",
            "/system/sd/xbin/",
            "/system/usr/we-need-root/",
            "/system/xbin/",
            "/cache",
            "/data",
            "/dev"
    };
}