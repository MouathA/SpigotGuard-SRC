package xyz.yooniks.spigotguard.helper;

import java.io.*;
import java.net.*;

public final class URIHelper
{
    private URIHelper() {
    }
    
    public static String readContent(final URL url) throws Exception {
        final StringBuilder sb = new StringBuilder();
        final URLConnection openConnection = url.openConnection();
        openConnection.setConnectTimeout(7500);
        openConnection.setReadTimeout(7500);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openConnection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        bufferedReader.close();
        return String.valueOf(sb);
    }
}
