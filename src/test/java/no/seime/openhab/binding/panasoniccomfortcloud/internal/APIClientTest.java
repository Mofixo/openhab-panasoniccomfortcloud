package no.seime.openhab.binding.panasoniccomfortcloud.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorage;

import com.google.gson.reflect.TypeToken;

import no.seime.openhab.binding.panasoniccomfortcloud.internal.dto.GetGroupsRequest;
import no.seime.openhab.binding.panasoniccomfortcloud.internal.dto.GetGroupsResponse;

@ExtendWith(MockitoExtension.class)
public class APIClientTest {

    private Storage storage = new VolatileStorage();

    // Disabled - must have credentials to run
    // @Test
    public void test() throws PanasonicComfortCloudException {
        ApiBridge apiBridge = new ApiBridge(storage);

        String username = "EMAIL USERNAME";
        String password = "PASSWORD";

        apiBridge.init(username, password, null);
        apiBridge.sendRequest(new GetGroupsRequest(), new TypeToken<GetGroupsResponse>() {
        }.getType());
    }

    @Test
    public void testSha256() throws NoSuchAlgorithmException {
        String hash = ApiBridge.generateHash("XZlJsY7dnp32w4KCC39xehdfsENsR265TjzHGQoePVP");
        assertEquals("TeFR13C1atlTUPWD1G9NdFCwvNC0Z0yOb7oVI8yjzvk", hash);
    }

    @Test
    public void testGenerateRandom() {
        String random = ApiBridge.generateRandomString(20);
        assertEquals(20, random.length());
    }

    @Test
    public void testParseItunes() throws IOException {
        InputStream is = getClass().getResourceAsStream("/itunes_lookup.json");
        String json = new String(is.readAllBytes());

        ApiBridge apiBridge = new ApiBridge(storage);
        String appVersion = apiBridge.parseItunesAppVersion(json);
        assertEquals("2.1.0", appVersion);
    }

    @Test
    public void testCompareVersions() {
        assertTrue(ApiBridge.compareVersions("4.4.0", "4.3.0") > 0);
        assertTrue(ApiBridge.compareVersions("4.10.0", "4.9.1") > 0);
        assertTrue(ApiBridge.compareVersions("4.3", "4.3.1") < 0);
        assertEquals(0, ApiBridge.compareVersions("4.3.0", "4.3"));
        assertEquals(0, ApiBridge.compareVersions(" 4.4.0 ", "4.4.0"));
    }

    @Test
    public void testNewestVersion() {
        assertEquals("4.4.0", ApiBridge.newestVersion(null, "4.4.0"));
        assertEquals("4.5.0", ApiBridge.newestVersion("4.5.0", "4.4.0"));
        assertEquals("4.4.0", ApiBridge.newestVersion("4.3.0", "4.4.0"));
    }

    @Test
    public void testUpdateAppVersionSkipsLookupWhenAlreadySwitched() {
        ApiBridge apiBridge = new ApiBridge(storage);
        apiBridge.init("user", "password", "4.5.0");
        // A concurrent request already moved away from the rejected version - retry without asking the App Store
        assertTrue(apiBridge.updateAppVersionFromAppStore("4.3.0"));
    }

    @Test
    public void testGetAppVersionIntegration() {
        ApiBridge apiBridge = new ApiBridge(storage);
        String appVersion = apiBridge.getAppVersion();
        assertNotNull(appVersion);
        // Version should look like x.y.z
        assertTrue(appVersion.matches("\\d+\\.\\d+\\.\\d+"));
    }
}
