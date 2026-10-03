package com.simpleskins.simpleskins;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MojangFetchTest {
    @Test
    public void parsesProfileId() {
        assertEquals("853c80ef3c3749fdaa2e995e64833f6",
                MojangFetch.parseProfileId("{\"id\":\"853c80ef3c3749fdaa2e995e64833f6\",\"name\":\"Notch\"}"));
        assertNull(MojangFetch.parseProfileId("{\"errorMessage\":\"Couldn\\u0027t find\"}"));
    }

    @Test
    public void parsesTexturesProperty() {
        String body = "{\"properties\":[{\"name\":\"textures\",\"value\":\"dmFs\",\"signature\":\"c2ln\"}]}";
        MojangFetch.Skin skin = MojangFetch.parseTextures(body);
        assertTrue(skin != null && skin.value().equals("dmFs") && skin.signature().equals("c2ln"));
        assertNull(MojangFetch.parseTextures("{\"properties\":[]}"));
    }

    @Test
    public void detectsSlimModel() {
        String classicJson = "{\"textures\":{\"SKIN\":{\"url\":\"http://x/y.png\"}}}";
        String slimJson = "{\"textures\":{\"SKIN\":{\"url\":\"http://x/y.png\",\"metadata\":{\"model\":\"slim\"}}}}";
        String classic = java.util.Base64.getEncoder().encodeToString(classicJson.getBytes());
        String slim = java.util.Base64.getEncoder().encodeToString(slimJson.getBytes());
        assertFalse(MojangFetch.slimModel(classic));
        assertTrue(MojangFetch.slimModel(slim));
        assertFalse(MojangFetch.slimModel("not-base64!!!"));
    }

    @Test
    public void validatesUsernames() {
        assertTrue(MojangFetch.validName("rekrap2"));
        assertTrue(MojangFetch.validName("a_b-C9".replace("-", "")));
        assertFalse(MojangFetch.validName("ab"));
        assertFalse(MojangFetch.validName("has space"));
        assertFalse(MojangFetch.validName(null));
    }
}
