/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2022-2023
 * Copyright Honeypot Contributors (c) 2022-2023
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.utils;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

// Disable unstable API warnings to suppress warnings related to 1.20
@SuppressWarnings("UnstableApiUsage")
public class PhysicsUtil {

	// Hide implicit constructor
	PhysicsUtil() {

	}

	private static final List<Material> physicsUp = Arrays.asList(
			Material.ACACIA_BUTTON,
			Material.ACACIA_DOOR,
			Material.ACACIA_HANGING_SIGN,
			Material.ACACIA_PRESSURE_PLATE,
			Material.ACACIA_SAPLING,
			Material.ACACIA_SIGN,
			Material.ACACIA_TRAPDOOR,
			Material.ACACIA_WALL_HANGING_SIGN,
			Material.ACACIA_WALL_SIGN,
			Material.ACTIVATOR_RAIL,
			Material.ALLIUM,
			Material.AMETHYST_CLUSTER,
			Material.ANVIL,
			Material.ATTACHED_MELON_STEM,
			Material.ATTACHED_PUMPKIN_STEM,
			Material.AZALEA,
			Material.AZURE_BLUET,
			Material.BAMBOO_DOOR,
			Material.BAMBOO_HANGING_SIGN,
			Material.BAMBOO_PRESSURE_PLATE,
			Material.BAMBOO_SAPLING,
			Material.BAMBOO_SIGN,
			Material.BAMBOO_TRAPDOOR,
			Material.BAMBOO_WALL_HANGING_SIGN,
			Material.BAMBOO_WALL_SIGN,
			Material.BEETROOTS,
			Material.BELL,
			Material.BIG_DRIPLEAF,
			Material.BIG_DRIPLEAF_STEM,
			Material.BIRCH_BUTTON,
			Material.BIRCH_DOOR,
			Material.BIRCH_HANGING_SIGN,
			Material.BIRCH_PRESSURE_PLATE,
			Material.BIRCH_SAPLING,
			Material.BIRCH_SIGN,
			Material.BIRCH_TRAPDOOR,
			Material.BIRCH_WALL_HANGING_SIGN,
			Material.BIRCH_WALL_SIGN,
			Material.BLACK_BANNER,
			Material.BLACK_CANDLE,
			Material.BLACK_CANDLE_CAKE,
			Material.BLACK_CARPET,
			Material.BLUE_BANNER,
			Material.BLUE_CANDLE,
			Material.BLUE_CANDLE_CAKE,
			Material.BLUE_CARPET,
			Material.BLUE_ORCHID,
			Material.BROWN_BANNER,
			Material.BROWN_CANDLE,
			Material.BROWN_CANDLE_CAKE,
			Material.BROWN_CARPET,
			Material.BROWN_MUSHROOM,
			Material.BUDDING_AMETHYST,
			Material.CAKE,
			Material.CANDLE,
			Material.CARROTS,
			Material.CAVE_VINES,
			Material.CAVE_VINES_PLANT,
			Material.CHERRY_BUTTON,
			Material.CHERRY_DOOR,
			Material.CHERRY_HANGING_SIGN,
			Material.CHERRY_PRESSURE_PLATE,
			Material.CHERRY_SAPLING,
			Material.CHERRY_SIGN,
			Material.CHERRY_TRAPDOOR,
			Material.CHERRY_WALL_HANGING_SIGN,
			Material.CHERRY_WALL_SIGN,
			Material.COMPARATOR,
			Material.CORNFLOWER,
			Material.CRIMSON_FUNGUS,
			Material.CRIMSON_HANGING_SIGN,
			Material.CRIMSON_ROOTS,
			Material.CRIMSON_SIGN,
			Material.CRIMSON_WALL_HANGING_SIGN,
			Material.CRIMSON_WALL_SIGN,
			Material.CYAN_BANNER,
			Material.CYAN_CANDLE,
			Material.CYAN_CANDLE_CAKE,
			Material.CYAN_CARPET,
			Material.DANDELION,
			Material.DARK_OAK_BUTTON,
			Material.DARK_OAK_DOOR,
			Material.DARK_OAK_HANGING_SIGN,
			Material.DARK_OAK_PRESSURE_PLATE,
			Material.DARK_OAK_SAPLING,
			Material.DARK_OAK_SIGN,
			Material.DARK_OAK_TRAPDOOR,
			Material.DARK_OAK_WALL_HANGING_SIGN,
			Material.DARK_OAK_WALL_SIGN,
			Material.DEAD_BUSH,
			Material.DECORATED_POT,
			Material.DETECTOR_RAIL,
			Material.FERN,
			Material.FIRE,
			Material.FLOWERING_AZALEA,
			Material.FROGSPAWN,
			Material.FROG_SPAWN_EGG,
			Material.GLOW_LICHEN,
			Material.SHORT_GRASS,
			Material.GRAVEL,
			Material.GRAY_BANNER,
			Material.GRAY_CANDLE,
			Material.GRAY_CANDLE_CAKE,
			Material.GRAY_CARPET,
			Material.GREEN_BANNER,
			Material.GREEN_CANDLE,
			Material.GREEN_CANDLE_CAKE,
			Material.GREEN_CARPET,
			Material.HANGING_ROOTS,
			Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
			Material.IRON_DOOR,
			Material.JUNGLE_BUTTON,
			Material.JUNGLE_DOOR,
			Material.JUNGLE_HANGING_SIGN,
			Material.JUNGLE_PRESSURE_PLATE,
			Material.JUNGLE_SAPLING,
			Material.JUNGLE_SIGN,
			Material.JUNGLE_TRAPDOOR,
			Material.JUNGLE_WALL_HANGING_SIGN,
			Material.JUNGLE_WALL_SIGN,
			Material.LADDER,
			Material.LANTERN,
			Material.LARGE_FERN,
			Material.LEVER,
			Material.LIGHTNING_ROD,
			Material.LIGHT_BLUE_BANNER,
			Material.LIGHT_BLUE_CANDLE,
			Material.LIGHT_BLUE_CANDLE_CAKE,
			Material.LIGHT_BLUE_CARPET,
			Material.LIGHT_GRAY_BANNER,
			Material.LIGHT_GRAY_CANDLE,
			Material.LIGHT_GRAY_CANDLE_CAKE,
			Material.LIGHT_GRAY_CARPET,
			Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
			Material.LILAC,
			Material.LILY_OF_THE_VALLEY,
			Material.LILY_PAD,
			Material.LIME_BANNER,
			Material.LIME_CANDLE,
			Material.LIME_CANDLE_CAKE,
			Material.LIME_CARPET,
			Material.MAGENTA_BANNER,
			Material.MAGENTA_CANDLE,
			Material.MAGENTA_CANDLE_CAKE,
			Material.MAGENTA_CARPET,
			Material.MANGROVE_HANGING_SIGN,
			Material.MANGROVE_PROPAGULE,
			Material.MANGROVE_SIGN,
			Material.MANGROVE_WALL_HANGING_SIGN,
			Material.MANGROVE_WALL_SIGN,
			Material.MELON_STEM,
			Material.NETHER_PORTAL,
			Material.NETHER_SPROUTS,
			Material.NETHER_WART,
			Material.OAK_BUTTON,
			Material.OAK_DOOR,
			Material.OAK_HANGING_SIGN,
			Material.OAK_PRESSURE_PLATE,
			Material.OAK_SAPLING,
			Material.OAK_SIGN,
			Material.OAK_WALL_HANGING_SIGN,
			Material.OAK_WALL_SIGN,
			Material.ORANGE_BANNER,
			Material.ORANGE_CANDLE,
			Material.ORANGE_CANDLE_CAKE,
			Material.ORANGE_CARPET,
			Material.ORANGE_TULIP,
			Material.OXEYE_DAISY,
			Material.PEONY,
			Material.PINK_BANNER,
			Material.PINK_CANDLE,
			Material.PINK_CANDLE_CAKE,
			Material.PINK_CARPET,
			Material.PINK_PETALS,
			Material.PINK_TULIP,
			Material.PITCHER_CROP,
			Material.PITCHER_PLANT,
			Material.PITCHER_POD,
			Material.POINTED_DRIPSTONE,
			Material.POPPY,
			Material.POTATOES,
			Material.POWERED_RAIL,
			Material.PUMPKIN_STEM,
			Material.PURPLE_BANNER,
			Material.PURPLE_CANDLE,
			Material.PURPLE_CANDLE_CAKE,
			Material.PURPLE_CARPET,
			Material.RAIL,
			Material.REDSTONE_TORCH,
			Material.REDSTONE_WIRE,
			Material.RED_BANNER,
			Material.RED_CANDLE,
			Material.RED_CANDLE_CAKE,
			Material.RED_CARPET,
			Material.RED_MUSHROOM,
			Material.RED_SAND,
			Material.RED_TULIP,
			Material.REPEATER,
			Material.ROSE_BUSH,
			Material.SAND,
			Material.SCULK_VEIN,
			Material.SMALL_DRIPLEAF,
			Material.SNOW,
			Material.SOUL_LANTERN,
			Material.SOUL_TORCH,
			Material.SOUL_WALL_TORCH,
			Material.SPORE_BLOSSOM,
			Material.SPRUCE_BUTTON,
			Material.SPRUCE_DOOR,
			Material.SPRUCE_HANGING_SIGN,
			Material.SPRUCE_PRESSURE_PLATE,
			Material.SPRUCE_SAPLING,
			Material.SPRUCE_SIGN,
			Material.SPRUCE_TRAPDOOR,
			Material.SPRUCE_WALL_HANGING_SIGN,
			Material.SPRUCE_WALL_SIGN,
			Material.STONE_BUTTON,
			Material.STONE_PRESSURE_PLATE,
			Material.SUNFLOWER,
			Material.SWEET_BERRY_BUSH,
			Material.TALL_GRASS,
			Material.TORCH,
			Material.TORCHFLOWER,
			Material.TORCHFLOWER_CROP,
			Material.TRIPWIRE,
			Material.TWISTING_VINES,
			Material.TWISTING_VINES_PLANT,
			Material.WARPED_FUNGUS,
			Material.WARPED_HANGING_SIGN,
			Material.WARPED_ROOTS,
			Material.WARPED_SIGN,
			Material.WARPED_WALL_HANGING_SIGN,
			Material.WARPED_WALL_SIGN,
			Material.WEEPING_VINES,
			Material.WEEPING_VINES_PLANT,
			Material.WHEAT,
			Material.WHITE_BANNER,
			Material.WHITE_CANDLE,
			Material.WHITE_CANDLE_CAKE,
			Material.WHITE_CARPET,
			Material.WHITE_TULIP,
			Material.WITHER_ROSE,
			Material.YELLOW_BANNER,
			Material.YELLOW_CANDLE,
			Material.YELLOW_CANDLE_CAKE,
			Material.YELLOW_CARPET);

	private static final List<Material> physicsSide = Arrays.asList(
			Material.ACACIA_BUTTON,
			Material.ACACIA_HANGING_SIGN,
			Material.ACACIA_SIGN,
			Material.ACACIA_TRAPDOOR,
			Material.ACACIA_WALL_HANGING_SIGN,
			Material.ACACIA_WALL_SIGN,
			Material.AMETHYST_CLUSTER,
			Material.BAMBOO_HANGING_SIGN,
			Material.BAMBOO_PRESSURE_PLATE,
			Material.BAMBOO_SIGN,
			Material.BAMBOO_TRAPDOOR,
			Material.BAMBOO_WALL_HANGING_SIGN,
			Material.BAMBOO_WALL_SIGN,
			Material.BIRCH_BUTTON,
			Material.BIRCH_HANGING_SIGN,
			Material.BIRCH_SIGN,
			Material.BIRCH_TRAPDOOR,
			Material.BIRCH_WALL_HANGING_SIGN,
			Material.BIRCH_WALL_SIGN,
			Material.BLACK_BANNER,
			Material.BLUE_BANNER,
			Material.BLUE_ORCHID,
			Material.BROWN_BANNER,
			Material.BUDDING_AMETHYST,
			Material.CAVE_VINES,
			Material.CAVE_VINES_PLANT,
			Material.CHERRY_BUTTON,
			Material.CHERRY_DOOR,
			Material.CHERRY_HANGING_SIGN,
			Material.CHERRY_PRESSURE_PLATE,
			Material.CHERRY_SAPLING,
			Material.CHERRY_SIGN,
			Material.CHERRY_TRAPDOOR,
			Material.CHERRY_WALL_HANGING_SIGN,
			Material.CHERRY_WALL_SIGN,
			Material.CRIMSON_FUNGUS,
			Material.CRIMSON_HANGING_SIGN,
			Material.CRIMSON_ROOTS,
			Material.CRIMSON_SIGN,
			Material.CRIMSON_WALL_HANGING_SIGN,
			Material.CRIMSON_WALL_SIGN,
			Material.CYAN_BANNER,
			Material.DARK_OAK_BUTTON,
			Material.DARK_OAK_HANGING_SIGN,
			Material.DARK_OAK_SIGN,
			Material.DARK_OAK_TRAPDOOR,
			Material.DARK_OAK_WALL_HANGING_SIGN,
			Material.DARK_OAK_WALL_SIGN,
			Material.GLOW_LICHEN,
			Material.GRAY_BANNER,
			Material.GREEN_BANNER,
			Material.JUNGLE_BUTTON,
			Material.JUNGLE_HANGING_SIGN,
			Material.JUNGLE_SIGN,
			Material.JUNGLE_TRAPDOOR,
			Material.JUNGLE_WALL_HANGING_SIGN,
			Material.JUNGLE_WALL_SIGN,
			Material.LADDER,
			Material.LEVER,
			Material.LIGHTNING_ROD,
			Material.LIGHT_BLUE_BANNER,
			Material.LIGHT_GRAY_BANNER,
			Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
			Material.LIME_BANNER,
			Material.MAGENTA_BANNER,
			Material.MANGROVE_HANGING_SIGN,
			Material.MANGROVE_SIGN,
			Material.MANGROVE_WALL_HANGING_SIGN,
			Material.MANGROVE_WALL_SIGN,
			Material.NETHER_PORTAL,
			Material.OAK_BUTTON,
			Material.OAK_HANGING_SIGN,
			Material.OAK_SIGN,
			Material.OAK_WALL_HANGING_SIGN,
			Material.OAK_WALL_SIGN,
			Material.ORANGE_BANNER,
			Material.PINK_BANNER,
			Material.PURPLE_BANNER,
			Material.RAIL,
			Material.REDSTONE_TORCH,
			Material.RED_BANNER,
			Material.SCULK_VEIN,
			Material.SOUL_WALL_TORCH,
			Material.SPORE_BLOSSOM,
			Material.SPRUCE_BUTTON,
			Material.SPRUCE_HANGING_SIGN,
			Material.SPRUCE_SIGN,
			Material.SPRUCE_TRAPDOOR,
			Material.SPRUCE_WALL_HANGING_SIGN,
			Material.SPRUCE_WALL_HANGING_SIGN,
			Material.SPRUCE_WALL_SIGN,
			Material.STONE_BUTTON,
			Material.TWISTING_VINES,
			Material.TWISTING_VINES_PLANT,
			Material.WALL_TORCH,
			Material.WARPED_FUNGUS,
			Material.WARPED_HANGING_SIGN,
			Material.WARPED_ROOTS,
			Material.WARPED_SIGN,
			Material.WARPED_WALL_HANGING_SIGN,
			Material.WARPED_WALL_SIGN,
			Material.WEEPING_VINES,
			Material.WEEPING_VINES_PLANT,
			Material.WHITE_BANNER,
			Material.YELLOW_BANNER);

	/**
	 * Get the list of blocks that have physics which can be placed on the sides of blocks (Trapdoors, as an example)
	 * @return Blocks that break when the block they're on break, but can be placed on the side
	 */
	public static List<Material> getSidePhysics() {
		return physicsSide;
	}

	/**
	 * Get the list of blocks that have physics but can only be placed on top of blocks (Flowers, as an example)
	 * @return Blocks that break when the block they're on break, but only can be placed on top
	 */
	public static List<Material> getUpPhysics() {
		return physicsUp;
	}

}