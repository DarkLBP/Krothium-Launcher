//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.technicpack.minecraftcore.launch;

import kml.StubLauncher;
import net.technicpack.launchercore.auth.UserModel;
import net.technicpack.launchercore.install.LauncherDirectories;
import net.technicpack.launchercore.launch.GameProcess;
import net.technicpack.launchercore.launch.ProcessExitListener;
import net.technicpack.launchercore.launch.java.JavaVersionRepository;
import net.technicpack.launchercore.modpacks.ModpackModel;
import net.technicpack.launchercore.modpacks.RunData;
import net.technicpack.minecraftcore.mojang.auth.MojangUser;
import net.technicpack.minecraftcore.mojang.version.MojangVersion;
import net.technicpack.minecraftcore.mojang.version.io.CompleteVersion;
import net.technicpack.minecraftcore.mojang.version.io.Library;
import net.technicpack.platform.IPlatformApi;
import net.technicpack.utilslib.OperatingSystem;
import net.technicpack.utilslib.Utils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MinecraftLauncher {
    private final LauncherDirectories directories;
    private final IPlatformApi platformApi;
    private final UserModel<MojangUser> userModel;
    private final JavaVersionRepository javaVersions;

    public MinecraftLauncher(IPlatformApi platformApi, LauncherDirectories directories, UserModel userModel, JavaVersionRepository javaVersions) {
        this.directories = directories;
        this.platformApi = platformApi;
        this.userModel = userModel;
        this.javaVersions = javaVersions;
    }

    public JavaVersionRepository getJavaVersions() {
        return this.javaVersions;
    }

    public GameProcess launch(ModpackModel pack, int memory, LaunchOptions options, CompleteVersion version) throws IOException {
        return this.launch(pack, (long) memory, options, null, version);
    }

    public GameProcess launch(ModpackModel pack, long memory, LaunchOptions options, ProcessExitListener exitListener, MojangVersion version) throws IOException {
        List<String> commands = this.buildCommands(pack, memory, version, options);
        StringBuilder full = new StringBuilder();
        boolean first = true;

        for (Iterator process = commands.iterator(); process.hasNext(); first = false) {
            String mcProcess = (String) process.next();
            if (!first) {
                full.append(" ");
            }

            full.append(mcProcess);
        }

        Utils.getLogger().info("Running " + full.toString());
        Process process1 = (new ProcessBuilder(commands)).directory(pack.getInstalledDirectory()).redirectErrorStream(true).start();
        GameProcess mcProcess1 = new GameProcess(commands, process1);
        if (exitListener != null) {
            mcProcess1.setExitListener(exitListener);
        }

        this.platformApi.incrementPackRuns(pack.getName());
        if (!Utils.sendTracking("runModpack", pack.getName(), pack.getInstalledVersion().getVersion(), options.getOptions().getClientId())) {
            Utils.getLogger().info("Failed to record event");
        }

        return mcProcess1;
    }

    private List<String> buildCommands(ModpackModel pack, long memory, MojangVersion version, LaunchOptions options) {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(this.javaVersions.getSelectedPath());
        OperatingSystem operatingSystem = OperatingSystem.getOperatingSystem();
        if (operatingSystem.equals(OperatingSystem.OSX)) {
            commands.add("-Xdock:icon=" + options.getIconPath());
            commands.add("-Xdock:name=" + pack.getDisplayName());
        } else if (operatingSystem.equals(OperatingSystem.WINDOWS)) {
            commands.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        }

        String launchJavaVersion = this.javaVersions.getSelectedVersion().getVersionNumber();
        short permSize = 128;
        if (memory >= 6144L) {
            permSize = 512;
        } else if (memory >= 2048L) {
            permSize = 256;
        }

        commands.add("-Xms" + memory + "m");
        commands.add("-Xmx" + memory + "m");
        if (!RunData.isJavaVersionAtLeast(launchJavaVersion, "1.8")) {
            commands.add("-XX:MaxPermSize=" + permSize + "m");
        }

        if (memory >= 4096L) {
            if (RunData.isJavaVersionAtLeast(launchJavaVersion, "1.7")) {
                commands.add("-XX:+UseG1GC");
                commands.add("-XX:MaxGCPauseMillis=4");
            } else {
                commands.add("-XX:+UseConcMarkSweepGC");
            }
        }

        commands.add("-Djava.library.path=" + (new File(pack.getBinDir(), "natives")).getAbsolutePath());
        commands.add("-Dfml.core.libraries.mirror=http://mirror.technicpack.net/Technic/lib/fml/%s");
        commands.add("-Dminecraft.applet.TargetDirectory=" + pack.getInstalledDirectory().getAbsolutePath());
        commands.add("-Djava.net.preferIPv4Stack=true");
        if (!options.getOptions().shouldUseStencilBuffer()) {
            commands.add("-Dforge.forceNoStencil=true");
        }

        String javaArguments = version.getJavaArguments();
        if (javaArguments != null && !javaArguments.isEmpty()) {
            commands.addAll(Arrays.asList(javaArguments.split(" ")));
        }

        commands.add("-cp");
        try {
            File stubPath = new File(StubLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            commands.add("\"" + stubPath.getAbsolutePath() + System.getProperty("path.separator") + this.buildClassPath(pack, version) + "\"");
            commands.add("kml.StubStarter");
            commands.add(version.getMainClass());
        } catch (Exception ex) {
            commands.add(this.buildClassPath(pack, version));
            commands.add(version.getMainClass());
        }
        commands.addAll(Arrays.asList(this.getMinecraftArguments(version, pack.getInstalledDirectory(), this.userModel.getCurrentUser())));
        options.appendToCommands(commands);
        return commands;
    }

    private String[] getMinecraftArguments(MojangVersion version, File gameDirectory, MojangUser mojangUser) {
        HashMap<String, String> map = new HashMap<>();
        StrSubstitutor substitutor = new StrSubstitutor(map);
        String[] split = version.getMinecraftArguments().split(" ");
        map.put("auth_username", mojangUser.getUsername());
        map.put("auth_session", mojangUser.getSessionId());
        map.put("auth_access_token", mojangUser.getAccessToken());
        map.put("auth_player_name", mojangUser.getDisplayName());
        map.put("auth_uuid", mojangUser.getProfile().getId());
        map.put("profile_name", mojangUser.getDisplayName());
        map.put("version_name", version.getId());
        map.put("game_directory", gameDirectory.getAbsolutePath());
        String targetAssets = this.directories.getAssetsDirectory().getAbsolutePath();
        String assetsKey = version.getAssetsKey();
        if (assetsKey == null || assetsKey.isEmpty()) {
            assetsKey = "legacy";
        }

        if (version.getAreAssetsVirtual()) {
            targetAssets = targetAssets + File.separator + "virtual" + File.separator + assetsKey;
        }

        map.put("game_assets", targetAssets);
        map.put("assets_root", targetAssets);
        map.put("assets_index_name", assetsKey);
        map.put("user_type", mojangUser.getProfile().isLegacy() ? "legacy" : "mojang");
        map.put("user_properties", mojangUser.getUserPropertiesAsJson());

        for (int i = 0; i < split.length; ++i) {
            split[i] = substitutor.replace(split[i]);
        }

        return split;
    }

    private String buildClassPath(ModpackModel pack, MojangVersion version) {
        StringBuilder result = new StringBuilder();
        String separator = System.getProperty("path.separator");
        File modpack = new File(pack.getBinDir(), "modpack.jar");
        if (modpack.exists()) {
            if (result.length() > 1) {
                result.append(separator);
            }

            result.append(modpack.getAbsolutePath());
        }

        Iterator minecraft = version.getLibrariesForOS().iterator();

        while (true) {
            Library library;
            do {
                do {
                    do {
                        if (!minecraft.hasNext()) {
                            File minecraft1 = new File(pack.getBinDir(), "minecraft.jar");
                            if (!minecraft1.exists()) {
                                throw new RuntimeException("Minecraft not installed for this pack: " + pack);
                            }

                            if (result.length() > 1) {
                                result.append(separator);
                            }

                            result.append(minecraft1.getAbsolutePath());
                            return result.toString();
                        }

                        library = (Library) minecraft.next();
                    }
                    while (library.getNatives() != null);
                }
                while (library.getName().startsWith("net.minecraftforge:minecraftforge"));
            }
            while (library.getName().startsWith("net.minecraftforge:forge"));

            File file = new File(this.directories.getCacheDirectory(), library.getArtifactPath().replace("${arch}", System.getProperty("sun.arch.data.model")));
            if (!file.isFile() || !file.exists()) {
                throw new RuntimeException("Library " + library.getName() + " not found.");
            }

            if (result.length() > 1) {
                result.append(separator);
            }

            result.append(file.getAbsolutePath());
        }
    }
}
