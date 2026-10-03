package com.simpleskins.simpleskins;

import org.junit.Test;

/**
 * Temporary oracle round 2: full PlayerInfo method list.
 */
public class SkinOracleTest {
    @Test
    public void dumpSkinMembers() {
        try {
            Class<?> found = Class.forName("net.minecraft.client.multiplayer.PlayerInfo");
            System.out.println("ORACLE2-CLASS " + found.getName());
            for (java.lang.reflect.Method method : found.getDeclaredMethods()) {
                StringBuilder params = new StringBuilder();
                for (Class<?> p : method.getParameterTypes()) {
                    if (params.length() > 0) {
                        params.append(",");
                    }
                    params.append(p.getSimpleName());
                }
                System.out.println("ORACLE2-METHOD " + method.getReturnType().getSimpleName() + " "
                        + method.getName() + "(" + params + ")");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ORACLE2-MISSING");
        }
    }
}
