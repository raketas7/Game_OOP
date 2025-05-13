package profiling;

import gui.MainApplicationFrame;
import gui.profiling.ProfileManager;
import log.LogWindowSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class ProfileIntegrationTest {
    private MainApplicationFrame frame;
    private MockedStatic<ProfileManager> mockedProfileManager;

    @Before
    public void setUp() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        LogWindowSource logSource = new LogWindowSource(10);

        mockedProfileManager = Mockito.mockStatic(ProfileManager.class);
        mockedProfileManager.when(() -> ProfileManager.checkAndLoadProfile(any(), any()))
                .thenAnswer(invocation -> null);

        frame = new MainApplicationFrame(bundle, logSource);
    }

    @After
    public void tearDown() {
        if (mockedProfileManager != null) {
            mockedProfileManager.close();
        }
    }

    @Test
    public void saveAndRestore_ShouldPreserveWindowStates() throws Exception {
        String profileName = "integration_test";
        frame.saveCurrentState(profileName);
        frame.applyProfileState(ProfileManager.loadProfile(profileName));
        assertNotNull(frame.getInternalWindows().get("logWindow"));
        assertNotNull(frame.getInternalWindows().get("gameWindow"));
    }
}