import log.LogChangeListener;
import log.LogEntry;
import log.LogLevel;
import log.LogWindowSource;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;

public class LogWindowSourceTest {

    private LogWindowSource logWindowSource;
    private static final int QUEUE_LENGTH = 5;

    @Before
    public void setUp() {
        logWindowSource = new LogWindowSource(QUEUE_LENGTH);
    }

    @Test
    public void testLogOverflow() {
        for (int i = 0; i < QUEUE_LENGTH * 2; i++) {
            logWindowSource.append(LogLevel.Debug, "Message " + i);
        }

        assertEquals(QUEUE_LENGTH, logWindowSource.size());

        Iterable<LogEntry> entries = logWindowSource.all();
        int index = QUEUE_LENGTH;
        for (LogEntry entry : entries) {
            assertEquals("Message " + index, entry.getMessage());
            index++;
        }
    }

    @Test
    public void testListenerLeak() {
        LogChangeListener listener = () -> {
        };

        logWindowSource.registerListener(listener);
        logWindowSource.unregisterListener(listener);

        try {
            java.lang.reflect.Field field = LogWindowSource.class.getDeclaredField("m_listeners");
            field.setAccessible(true);
            ArrayList<LogChangeListener> listeners = (ArrayList<LogChangeListener>) field.get(logWindowSource);
            assertFalse(listeners.contains(listener));
        } catch (Exception e) {
            fail("Exception during reflection: " + e.getMessage());
        }
    }
}
