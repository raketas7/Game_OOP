package profiling;

import gui.MainApplicationFrame;
import gui.windows.GameWindow;
import gui.GameMechanics.Player;
import gui.GameMechanics.Achievement;
import gui.Visuals.GameVisualizer;
import gui.profiling.ProfileManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProfileManagerTest {
    private static final String TEST_PROFILE = "testProfile";
    private static final String INVALID_NAME = "invalid/name";

    private MainApplicationFrame frame;
    private ResourceBundle bundle;
    private GameWindow gameWindow;
    private Player player;
    private GameVisualizer visualizer;
    private Preferences preferences;

    @Before
    public void setUp() {
        File dir = new File(ProfileManager.PROFILES_DIR);
        if (dir.exists()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                file.delete();
            }
        }

        frame = mock(MainApplicationFrame.class);
        bundle = mock(ResourceBundle.class);
        gameWindow = mock(GameWindow.class);
        player = mock(Player.class);
        visualizer = mock(GameVisualizer.class);
        preferences = mock(Preferences.class);

        when(frame.getInternalWindows()).thenReturn(Map.of("gameWindow", gameWindow));
        when(gameWindow.getPlayer()).thenReturn(player);
        when(gameWindow.getGameVisualizer()).thenReturn(visualizer);
        when(visualizer.getAchievements()).thenReturn(new ArrayList<>());
        when(bundle.getString(anyString())).thenReturn("mocked_string");
    }

    @Test
    public void saveProfile_ShouldCreateValidJsonFile() throws IOException {
        Map<String, Object> testData = new HashMap<>();
        testData.put("window1", Map.of("x", 100, "y", 100, "width", 300, "height", 400));
        ProfileManager.saveProfile(TEST_PROFILE, testData);
        File file = new File(ProfileManager.PROFILES_DIR + File.separator + TEST_PROFILE + ".json");
        assertTrue(file.exists());
    }

    @Test
    public void saveProfile_ShouldThrowForInvalidName() {
        Map<String, Object> testData = new HashMap<>();
        assertThrows(IOException.class, () -> ProfileManager.saveProfile(INVALID_NAME, testData));
    }

    @Test
    public void loadProfile_ShouldReturnCorrectData() throws IOException, ParseException {
        Map<String, Object> testData = new HashMap<>();
        testData.put("window1", Map.of("x", 100, "y", 100, "width", 300, "height", 400));
        ProfileManager.saveProfile(TEST_PROFILE, testData);
        Map<String, Object> loadedData = ProfileManager.loadProfile(TEST_PROFILE);
        assertEquals(testData, loadedData);
    }

    @Test
    public void loadProfile_ShouldThrowForMissingFile() {
        assertThrows(IOException.class, () -> ProfileManager.loadProfile("nonexistent"));
    }

    @Test
    public void getAvailableProfiles_ShouldReturnSavedProfiles() throws IOException {
        ProfileManager.saveProfile(TEST_PROFILE, new HashMap<>());
        List<String> profiles = ProfileManager.getAvailableProfiles();
        assertTrue(profiles.contains(TEST_PROFILE));
    }

    @Test
    public void getAvailableProfiles_ShouldReturnEmptyListIfNoProfiles() {
        List<String> profiles = ProfileManager.getAvailableProfiles();
        assertTrue(profiles.isEmpty());
    }

    @Test
    public void isValidProfileName_ShouldRejectInvalidNames() {
        assertFalse(ProfileManager.isValidProfileName("invalid/name"));
        assertFalse(ProfileManager.isValidProfileName("invalid.name"));
        assertFalse(ProfileManager.isValidProfileName(""));
    }

    @Test
    public void isValidProfileName_ShouldAcceptValidNames() {
        assertTrue(ProfileManager.isValidProfileName("valid_name"));
        assertTrue(ProfileManager.isValidProfileName("valid-name"));
        assertTrue(ProfileManager.isValidProfileName("Valid Name 123"));
    }

    @Test
    public void checkAndLoadProfile_NoProfiles() throws BackingStoreException {
        try (MockedStatic<JOptionPane> mockedJOptionPane = mockStatic(JOptionPane.class);
             MockedStatic<Preferences> mockedPreferences = mockStatic(Preferences.class)) {
            mockedJOptionPane.when(() -> JOptionPane.showOptionDialog(any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                    .thenReturn(JOptionPane.NO_OPTION);
            mockedPreferences.when(() -> Preferences.userNodeForPackage(Player.class)).thenReturn(preferences);

            when(player.getCoins()).thenReturn(50);
            Achievement achievement = mock(Achievement.class);
            when(visualizer.getAchievements()).thenReturn(List.of(achievement));

            ProfileManager.checkAndLoadProfile(frame, bundle);

            verify(player).addCoins(-50);
            verify(player).saveCoins();
            verify(player).setEnemiesKilled(0);
            verify(achievement).reset();
            verify(visualizer).updateAchievementsPanel();
            verify(visualizer).resetGame();
            verify(preferences).clear();
        }
    }
}