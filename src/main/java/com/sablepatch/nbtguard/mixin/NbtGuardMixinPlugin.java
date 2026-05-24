package com.sablepatch.nbtguard.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin plugin that conditionally applies mixins only if the target mods are loaded.
 *
 * IMPORTANT: Do NOT use Class.forName() in onLoad() — it triggers early class loading
 * of transitive dependencies (e.g. ServerLevel), which causes other mods' mixins
 * (like Veil's NetworkServerLevelMixin) to fail with "target was loaded too early".
 */
public class NbtGuardMixinPlugin implements IMixinConfigPlugin {

    private Boolean sableLoaded = null;
    private Boolean simulatedLoaded = null;

    @Override
    public void onLoad(String mixinPackage) {
        // Intentionally empty — do NOT call Class.forName() here.
        // Deferred to shouldApplyMixin() where class loading is safe.
    }

    /**
     * Checks if a class exists without triggering eager class loading.
     * Uses ClassLoader.getResource() which only checks file presence.
     */
    private boolean checkClassExists(String className) {
        String path = className.replace('.', '/') + ".class";
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = NbtGuardMixinPlugin.class.getClassLoader();
        return cl.getResource(path) != null;
    }

    private boolean isSableLoaded() {
        if (sableLoaded == null) {
            sableLoaded = checkClassExists("dev.ryanhcode.sable.api.SubLevelAssemblyHelper");
        }
        return sableLoaded;
    }

    private boolean isSimulatedLoaded() {
        if (simulatedLoaded == null) {
            simulatedLoaded = checkClassExists("dev.simulated_team.simulated.util.SimAssemblyHelper");
        }
        return simulatedLoaded;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("SubLevelAssemblyHelperMixin")) {
            return isSableLoaded();
        }
        if (mixinClassName.contains("SubLevelMoveBlocksMixin")) {
            return isSableLoaded();
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
