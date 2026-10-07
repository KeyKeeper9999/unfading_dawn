package com.keykeeper.dushi.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.keykeeper.dushi.Dushi;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A building as a grid of blocks, written by tools/gen_structures.py into data/unfading_dawn/plan/NAME.txt:
 * a palette of block states (a chest also names its loot table) and one palette index per cell, run-length encoded,
 * layer by layer from the bottom, each layer row by row from north to south, each row from west to east.
 */
public final class Plan {
    /** A cell the building leaves as the world made it. */
    public static final short SKIP = -1;

    private static final Map<String, Plan> LOADED = new HashMap<>();

    public final int width;
    public final int height;
    public final int depth;
    /** The cell that lands on the spot the structure is generated at; its layer is the one feet stand in. */
    public final int originX;
    public final int originY;
    public final int originZ;
    public final BlockState[] palette;
    /** Loot table of the chest with the same palette index, or null. */
    public final ResourceKey<LootTable>[] loot;
    private final short[] cells;

    private Plan(int width, int height, int depth, int originX, int originY, int originZ, BlockState[] palette, ResourceKey<LootTable>[] loot, short[] cells) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.palette = palette;
        this.loot = loot;
        this.cells = cells;
    }

    public short cell(int x, int y, int z) {
        return this.cells[(y * this.depth + z) * this.width + x];
    }

    public static synchronized Plan get(String name) {
        return LOADED.computeIfAbsent(name, Plan::load);
    }

    /** Reads a plan from a file outside the mod (the map builder's) and makes it known under {@code name}. */
    public static synchronized Plan fromFile(String name, Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Plan plan = read(name, reader);
            LOADED.put(name, plan);
            return plan;
        } catch (IOException | CommandSyntaxException e) {
            throw new IllegalStateException("cannot read plan " + file, e);
        }
    }

    private static Plan load(String name) {
        String path = "/data/" + Dushi.MODID + "/plan/" + name + ".txt";
        try (InputStream stream = Dushi.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("no plan " + path);
            }
            return read(name, new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
        } catch (IOException | CommandSyntaxException e) {
            throw new IllegalStateException("cannot read plan " + name, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Plan read(String name, BufferedReader reader) throws IOException, CommandSyntaxException {
        {
            String[] size = reader.readLine().split(" ");
            String[] origin = reader.readLine().split(" ");
            int width = Integer.parseInt(size[1]);
            int height = Integer.parseInt(size[2]);
            int depth = Integer.parseInt(size[3]);
            int count = Integer.parseInt(reader.readLine().split(" ")[1]);
            BlockState[] palette = new BlockState[count];
            ResourceKey<LootTable>[] loot = new ResourceKey[count];
            for (int i = 0; i < count; i++) {
                String line = reader.readLine();
                String state = line;
                if (line.startsWith("chest ")) {
                    int space = line.indexOf(' ', 6);
                    loot[i] = ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(line.substring(6, space)));
                    state = line.substring(space + 1);
                }
                palette[i] = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, state, false).blockState();
            }
            short[] cells = new short[width * height * depth];
            int at = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                StringTokenizer tokens = new StringTokenizer(line, " ");
                while (tokens.hasMoreTokens()) {
                    String token = tokens.nextToken();
                    int star = token.indexOf('*');
                    int run = star < 0 ? 1 : Integer.parseInt(token.substring(0, star));
                    String value = star < 0 ? token : token.substring(star + 1);
                    short index = value.equals("_") ? SKIP : Short.parseShort(value);
                    for (int i = 0; i < run; i++) {
                        cells[at++] = index;
                    }
                }
            }
            if (at != cells.length) {
                throw new IllegalStateException("plan " + name + " has " + at + " cells, expected " + cells.length);
            }
            return new Plan(width, height, depth, Integer.parseInt(origin[1]), Integer.parseInt(origin[2]), Integer.parseInt(origin[3]), palette, loot, cells);
        }
    }
}
