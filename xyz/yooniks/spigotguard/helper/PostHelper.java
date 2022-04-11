package xyz.yooniks.spigotguard.helper;

import java.nio.charset.*;
import java.net.*;
import java.io.*;

public class PostHelper
{
    public static String post(final String s, final String s2) throws IOException {
        final byte[] bytes = s2.getBytes(StandardCharsets.UTF_8);
        final HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(s).openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("User-Agent", "Java client");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        final DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
        try {
            dataOutputStream.write(bytes);
            dataOutputStream.close();
        }
        catch (Throwable t) {
            try {
                dataOutputStream.close();
            }
            catch (Throwable t2) {
                t.addSuppressed(t2);
            }
            throw t;
        }
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder sb;
        try {
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            bufferedReader.close();
        }
        catch (Throwable t3) {
            try {
                bufferedReader.close();
            }
            catch (Throwable t4) {
                t3.addSuppressed(t4);
            }
            throw t3;
        }
        return String.valueOf(sb);
    }
}
