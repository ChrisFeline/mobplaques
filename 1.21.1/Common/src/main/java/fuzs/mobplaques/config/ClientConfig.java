package fuzs.mobplaques.config;

import fuzs.mobplaques.MobPlaques;
import fuzs.mobplaques.client.gui.plaque.MobPlaqueRenderer;
import fuzs.mobplaques.client.handler.MobPlaqueHandler;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.ValueCallback;
import fuzs.puzzleslib.api.config.v3.serialization.ConfigDataSet;
import fuzs.puzzleslib.api.config.v3.serialization.KeyedValueProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Map;

public class ClientConfig implements ConfigCore {
    private static final String KEY_GENERAL_CATEGORY = "general";

    public ModConfigSpec.ConfigValue<Boolean> allowRendering;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Hide all plaques when mob has full health.")
    public boolean hideAtFullHealth = false;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Show plaques for the entity picked by the crosshair only.")
    public boolean pickedEntity = false;
    @Config(category = KEY_GENERAL_CATEGORY, description = {"The raytrace range for finding a picked entity.", "Setting this to -1 will make it use the player entity interaction range, which is 3 in survival."})
    @Config.IntRange(min = -1, max = 64)
    public int pickedEntityInteractionRange = -1;
    @Config(category = KEY_GENERAL_CATEGORY, description = {
            "Coyote time in seconds after which a no longer picked entity will still show the plaques.", "Set to -1 to keep the old entity until a new one is picked by the crosshair."
    })
    @Config.IntRange(min = -1)
    public int pickedEntityDelay = 2;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Custom scale for rendering plaques.")
    @Config.DoubleRange(min = 0.05, max = 2.0)
    public double plaqueScale = 0.5;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Amount of pixels a row of plaques may take up, when exceeding this value a new row will be started.")
    @Config.IntRange(min = 0)
    public int maxPlaqueRowWidth = 108;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Render mob plaques below the mob's name tag instead of above.")
    public boolean renderBelowNameTag = false;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Show a black background box behind plaques. Disabled by default as it doesn't work with shaders.")
    public boolean plaqueBackground = true;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Always render plaques with full brightness to be most visible, ignoring local lighting conditions.")
    public FullBrightnessRendering fullBrightness = FullBrightnessRendering.UNOBSTRUCTED;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Height offset from default position.")
    public int heightOffset = 0;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Distance to the mob at which plaques will still be visible. The distance is halved when the mob is crouching.")
    @Config.IntRange(min = 0)
    public int maxRenderDistance = 48;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Show plaques from mobs obstructed by walls the player cannot see through, similar to the nameplates of other players.")
    public boolean behindWalls = true;
    @Config(category = KEY_GENERAL_CATEGORY, description = "Dynamically increase plaque size the further away the camera is to simplify readability.")
    public boolean scaleWithDistance = true;
    @Config(category = KEY_GENERAL_CATEGORY, name = "mob_blacklist", description = {"Entities blacklisted from showing any plaques.", ConfigDataSet.CONFIG_DESCRIPTION})
    List<String> mobBlacklistRaw = KeyedValueProvider.toString(Registries.ENTITY_TYPE, EntityType.ARMOR_STAND);
    @Config(category = KEY_GENERAL_CATEGORY, name = "disallowed_mob_selectors", description = {"Selectors for choosing mobs to prevent rendering plaques for, takes priority over allowed list."})
    @Config.AllowedValues(values = {"mobplaques:all", "mobplaques:tamed", "mobplaques:tamed_only_owner", "mobplaques:player", "mobplaques:monster", "mobplaques:boss", "mobplaques:mount"})
    List<String> disallowedMobSelectorsRaw = KeyedValueProvider.toString(MobPlaquesSelector.class, MobPlaques.MOD_ID);
    @Config(category = KEY_GENERAL_CATEGORY, name = "allowed_mob_selectors", description = {"Selectors for choosing mobs to render plaques for."})
    @Config.AllowedValues(values = {"mobplaques:all", "mobplaques:tamed", "mobplaques:tamed_only_owner", "mobplaques:player", "mobplaques:monster", "mobplaques:boss", "mobplaques:mount"})
    List<String> allowedMobSelectorsRaw = KeyedValueProvider.toString(MobPlaquesSelector.class,
            MobPlaques.MOD_ID, MobPlaquesSelector.ALL);

    public ConfigDataSet<EntityType<?>> mobBlacklist;
    public ConfigDataSet<MobPlaquesSelector> disallowedMobSelectors;
    public ConfigDataSet<MobPlaquesSelector> allowedMobSelectors;

    @Override
    public void addToBuilder(ModConfigSpec.Builder builder, ValueCallback callback) {
        builder.push(KEY_GENERAL_CATEGORY);
        // we need this here to be able to set a value from pressing the key
        this.allowRendering = builder.comment("Are mob plaques enabled, toggleable in-game using the 'J' key by default.").define("allow_rendering", true);
        builder.pop();
        for (Map.Entry<ResourceLocation, MobPlaqueRenderer> entry : MobPlaqueHandler.PLAQUE_RENDERERS.entrySet()) {
            builder.push(entry.getKey().getPath());
            entry.getValue().setupConfig(builder, callback);
            builder.pop();
        }
    }

    @Override
    public void afterConfigReload() {
        this.mobBlacklist = ConfigDataSet.from(Registries.ENTITY_TYPE, this.mobBlacklistRaw);
        // manually process enum lists as night config keeps values as strings, making it hard to deal with as generic type suggests an enum value
        this.disallowedMobSelectors = ConfigDataSet.from(KeyedValueProvider.enumConstants(MobPlaquesSelector.class,
                MobPlaques.MOD_ID), this.disallowedMobSelectorsRaw);
        this.allowedMobSelectors = ConfigDataSet.from(KeyedValueProvider.enumConstants(MobPlaquesSelector.class,
                MobPlaques.MOD_ID), this.allowedMobSelectorsRaw);
    }

    public enum FullBrightnessRendering {
        ALWAYS, UNOBSTRUCTED, NEVER
    }
}
