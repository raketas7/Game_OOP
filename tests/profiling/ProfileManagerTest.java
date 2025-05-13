package profiling;

import gui.profiling.ProfileManager;
import org.junit.Before;
import org.junit.Test;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileManagerTest {
    private static final String TEST_PROFILE = "testProfile";
    private static final String INVALID_NAME = "invalid/name";

    @Before
    public void setUp() {
        File dir = new File(ProfileManager.PROFILES_DIR);
        if (dir.exists()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                file.delete();
            }
        }
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
}