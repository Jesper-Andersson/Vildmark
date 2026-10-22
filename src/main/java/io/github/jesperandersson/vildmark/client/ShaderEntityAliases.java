package io.github.jesperandersson.vildmark.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
//import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;

import java.util.Map;

/**
 * Gives Vildmark entities the shader entity IDs of matching vanilla entities, so shader packs that
 * only list vanilla entities in their entity.properties still light them (glow, bloom, light
 * emission). Only loaded through the Iris mixin, so referencing Iris classes here is safe.
 */
public final class ShaderEntityAliases {
    // Vildmark entity -> vanilla entity whose shader ID it borrows
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("willothewisp", "allay")
    );

    private ShaderEntityAliases() {}

    /**
     * Returns the pack's map plus IDs for Vildmark entities it didn't assign itself, copied from
     * the vanilla template entities. Iris hands out an unmodifiable map, so this builds a new one.
     */
    /*
    public static Object2IntMap<NamespacedId> alias(Object2IntMap<NamespacedId> shaderIds) {
        Object2IntMap<NamespacedId> aliasedIds = new Object2IntOpenHashMap<>(shaderIds);
        aliasedIds.defaultReturnValue(shaderIds.defaultReturnValue());
        ALIASES.forEach((entity, template) -> {
            NamespacedId id = new NamespacedId("vildmark", entity);
            NamespacedId templateId = new NamespacedId("minecraft", template);
            if (!aliasedIds.containsKey(id) && aliasedIds.containsKey(templateId)) {
                aliasedIds.put(id, aliasedIds.getInt(templateId));
            }
        });
        return Object2IntMaps.unmodifiable(aliasedIds);
    }
     */
}
