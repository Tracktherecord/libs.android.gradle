package libs.android.gradle;

public class DeviceInfoPlugin {

    public void installPlugin() {
        System.out.println("Plugin installed successfully.");
    }

    public static void main(String[] args) {
        DeviceInfoPlugin plugin = new DeviceInfoPlugin();
        plugin.installPlugin();
    }
}

