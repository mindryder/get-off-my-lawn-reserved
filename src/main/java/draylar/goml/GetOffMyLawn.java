package draylar.goml;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import draylar.goml.cca.ClaimComponent;
import draylar.goml.cca.WorldClaimComponent;
import draylar.goml.other.ClaimCommand;
import draylar.goml.config.GOMLConfig;
import draylar.goml.registry.GOMLBlocks;
import draylar.goml.registry.GOMLEntities;
import draylar.goml.registry.GOMLItems;
import eu.pb4.polymer.api.item.PolymerItemGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetOffMyLawn implements ModInitializer, WorldComponentInitializer {

	public static final ComponentKey<ClaimComponent> CLAIM = ComponentRegistryV3.INSTANCE.getOrCreate(id("claims"), ClaimComponent.class);
	public static GOMLConfig CONFIG = new GOMLConfig();
	public static final PolymerItemGroup GROUP = PolymerItemGroup.create(id("group"), new TranslatableText("itemGroup.goml.group"));
	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		GOMLBlocks.init();
		GOMLItems.init();
		GOMLEntities.init();
		EventHandlers.init();
		ClaimCommand.init();

		ServerLifecycleEvents.SERVER_STARTING.register((s) -> {
			GetOffMyLawn.CONFIG = GOMLConfig.loadOrCreateConfig();
		});

		GROUP.setIcon(new ItemStack(GOMLBlocks.WITHERED_CLAIM_ANCHOR.getSecond()));
	}

	public static Identifier id(String name) {
		return new Identifier("goml", name);
	}

	@Override
	public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
		registry.register(CLAIM, WorldClaimComponent::new);
	}
}
