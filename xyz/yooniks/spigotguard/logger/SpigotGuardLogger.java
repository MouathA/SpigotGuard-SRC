package xyz.yooniks.spigotguard.logger;

import xyz.yooniks.spigotguard.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;

public class SpigotGuardLogger
{
    private static final Executor executor;
    private static final Logger LOGGER;
    private static final File fileLog;
    
    static {
        LOGGER = ((SpigotGuardPlugin)SpigotGuardPlugin.getPlugin((Class)SpigotGuardPlugin.class)).getLogger();
        executor = Executors.newCachedThreadPool();
        fileLog = new File(SpigotGuardPlugin.getInstance().getDataFolder(), "logs.txt");
    }
    
    public static void exception(final String s, final Exception ex) {
        SpigotGuardLogger.LOGGER.log(Level.WARNING, s, ex);
    }
    
    public static void log(final Level level, final String s, final Object... array) {
        SpigotGuardLogger.LOGGER.log(level, s, array);
        SpigotGuardLogger.executor.execute(SpigotGuardLogger::lambda$log$0);
    }
    
    private static void lambda$log$0(final String s, final Object[] array) {
        try {
            final FileWriter fileWriter = new FileWriter(SpigotGuardLogger.fileLog, true);
            try {
                final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                try {
                    bufferedWriter.write(String.format(s, array));
                    bufferedWriter.write("\n");
                    bufferedWriter.close();
                }
                catch (Throwable t) {
                    try {
                        bufferedWriter.close();
                    }
                    catch (Throwable t2) {
                        t.addSuppressed(t2);
                    }
                    throw t;
                }
                fileWriter.close();
            }
            catch (Throwable t3) {
                try {
                    fileWriter.close();
                }
                catch (Throwable t4) {
                    t3.addSuppressed(t4);
                }
                throw t3;
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
