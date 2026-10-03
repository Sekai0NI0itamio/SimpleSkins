package com.simpleskins.simpleskins;

import org.junit.Test;

/**
 * Temporary oracle: prints the real 1.20.1 client skin member names from CI's
 * decompiled Minecraft so the skin applier uses exact field names.
 */
public class SkinOracleTest {
    @Test
    public void dumpSkinMembers() {
        dump("net.minecraft.client.multiplayer.PlayerInfo");
    }

    static void dump(String name) {
        try {
            Class<?> found = Class.forName(name);
            System.out.println("ORACLE-CLASS " + name);
            for (java.lang.reflect.Field field : found.getDeclaredFields()) {
                System.out.println("ORACLE-FIELD " + field.getType().getSimpleName() + " " + field.getName());
            }
            for (java.lang.reflect.Method method : found.getDeclaredMethods()) {
                String lower = method.getName().toLowerCase();
                if (lower.contains("skin") || lower.contains("model") || lower.contains("cape")) {
                    System.out.println("ORACLE-METHOD " + method.getReturnType().getSimpleName() + " " + method.getName());
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ORACLE-MISSING " + name);
        }
    }
}
