package com.simpleskins.simpleskins;

import org.junit.Test;

/**
 * Temporary oracle round 3: all void no-arg methods on PlayerInfo.
 */
public class SkinOracleTest {
    @Test
    public void dumpSkinMembers() {
        try {
            Class<?> found = Class.forName("net.minecraft.client.multiplayer.PlayerInfo");
            System.out.println("ORACLE3-CLASS " + found.getName());
            for (java.lang.reflect.Method method : found.getDeclaredMethods()) {
                if (method.getReturnType() == void.class && method.getParameterCount() == 0) {
                    System.out.println("ORACLE3-VOID-NOARG " + method.getName());
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ORACLE3-MISSING");
        }
    }
}
