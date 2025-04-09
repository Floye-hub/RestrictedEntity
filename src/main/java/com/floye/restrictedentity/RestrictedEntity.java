package com.floye.restrictedentity;

import com.floye.restrictedentity.command.RestrictedZoneCommand;
import com.floye.restrictedentity.config.ConfigLoader;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestrictedEntity implements ModInitializer {
	public static final String MOD_ID = "restrictedentity";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ConfigLoader.loadConfig();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			RestrictedZoneCommand.register(dispatcher);
		});
	}
}