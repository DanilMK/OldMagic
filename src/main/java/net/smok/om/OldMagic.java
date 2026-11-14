package net.smok.om;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.smok.om.command.ParticleBlockTemplateTest;
import net.smok.om.command.SpellCommand;
import net.smok.om.network.NetworkHandler;
import net.smok.om.spell.SpellTickManager;

public class OldMagic implements ModInitializer {


	@Override
	public void onInitialize() {

        Values.init();
        NetworkHandler.init();
        CommandRegistrationCallback.EVENT.register(new SpellCommand());
        CommandRegistrationCallback.EVENT.register(new ParticleBlockTemplateTest());
        ServerTickEvents.START_LEVEL_TICK.register(level -> {
            SpellTickManager.get(level).tick(level);
        });
	}

}
