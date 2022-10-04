﻿package emu.grasscutter.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import emu.grasscutter.GameConstants;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.ResourceLoader;
import emu.grasscutter.data.excels.AvatarData;
import emu.grasscutter.data.excels.ItemData;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.inventory.ItemType;
import emu.grasscutter.game.inventory.MaterialType;
import emu.grasscutter.game.props.MonsterType;
import emu.grasscutter.game.props.SceneType;
import emu.grasscutter.utils.Language;
import emu.grasscutter.utils.Language.TextStrings;
import emu.grasscutter.utils.SparseSet;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import static emu.grasscutter.config.Configuration.*;

public final class Tools {
    public static void createGmHandbooks() throws Exception {
        final List<Language> languages = Language.TextStrings.getLanguages();

        final Long2ObjectMap<TextStrings> textMaps = Language.getTextMapStrings();

        ResourceLoader.loadAll();


        SparseSet illegalWeaponIds = new SparseSet("""
        10000-10008, 11411, 11506-11508, 12505, 12506, 12508, 12509,
        13503, 13506, 14411, 14503, 14505, 14508, 15504-15506
        """);

        SparseSet illegalRelicIds = new SparseSet("""
        20001, 23300-23340, 23383-23385, 78310-78554, 99310-99554
        """);

        SparseSet illegalItemIds = new SparseSet("""
        100086, 100087, 100100-101000, 101106-101110, 101306, 101500-104000,
        105001, 105004, 106000-107000, 107011, 108000, 109000-110000,
        115000-130000, 200200-200899, 220050, 220054
        """);


        final Int2LongSortedMap avatarNames = new Int2LongRBTreeMap(GameData.getAvatarDataMap().int2ObjectEntrySet().stream().filter(e -> e.getIntKey() >= 10000002 && e.getIntKey() < 11000000).collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e -> e.getValue().getNameTextMapHash())));

        final Int2LongSortedMap virtualItems = new Int2LongRBTreeMap(GameData.getItemDataMap().int2ObjectEntrySet().stream().filter(e -> e.getValue().getItemType() == ItemType.ITEM_VIRTUAL && !illegalItemIds.contains(e.getIntKey())).collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e ->  e.getValue().getNameTextMapHash())));
        final Int2LongSortedMap weaponItems = new Int2LongRBTreeMap(GameData.getItemDataMap().int2ObjectEntrySet().stream().filter(e -> e.getValue().getItemType() == ItemType.ITEM_WEAPON && !illegalWeaponIds.contains(e.getIntKey())).collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e ->  e.getValue().getNameTextMapHash())));
        final Int2LongSortedMap furnitureItems = new Int2LongRBTreeMap(GameData.getItemDataMap().int2ObjectEntrySet().stream().filter(e -> e.getValue().getItemType() == ItemType.ITEM_FURNITURE && !illegalItemIds.contains(e.getIntKey())).collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e ->  e.getValue().getNameTextMapHash())));
//        final Int2LongSortedMap materials = new Int2LongRBTreeMap(GameData.getItemDataMap().int2ObjectEntrySet().stream().filter(e -> e.getValue().getItemType() == ItemType.ITEM_MATERIAL).collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e ->  e.getValue().getNameTextMapHash())));

        final var materialTypes = MaterialType.values();
        final List<Int2LongRBTreeMap> materialsNames = Arrays.stream(materialTypes).map(type ->
            new Int2LongRBTreeMap(
                GameData.getItemDataMap()
                    .int2ObjectEntrySet()
                    .stream()
                    .filter(
                        e -> e.getValue().getItemType() == ItemType.ITEM_MATERIAL
                            && e.getValue().getMaterialType() == type
                            && !illegalItemIds.contains(e.getIntKey())
                    ).collect(
                        Collectors.toMap(
                            Int2ObjectMap.Entry::getIntKey,
                            e -> e.getValue().getNameTextMapHash())
                    ))
        ).toList();

        final Int2LongSortedMap relicNames =  new Int2LongRBTreeMap(GameData.getItemDataMap().int2ObjectEntrySet().stream().filter(e -> e.getValue().getItemType() == ItemType.ITEM_RELIQUARY && !illegalRelicIds.contains(e.getIntKey())).collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e ->  e.getValue().getNameTextMapHash())));

        final var monsterTypes = MonsterType.values();
        final List<Int2LongRBTreeMap> monsterNames = Arrays.stream(monsterTypes).map(type ->
            new Int2LongRBTreeMap(
                GameData.getMonsterDataMap()
                    .int2ObjectEntrySet()
                    .stream()
                    .filter(
                        e -> e.getValue().getType() == type
                    ).collect(
                        Collectors.toMap(
                            Int2ObjectMap.Entry::getIntKey,
                            e ->  e.getValue().getNameTextMapHash())
                    ))
        ).toList();


//        final Int2LongSortedMap monsterNames = new Int2LongRBTreeMap(GameData.getMonsterDataMap().int2ObjectEntrySet().stream().filter(e -> e.getValue().getType()).collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e ->  e.getValue().getNameTextMapHash())));
        final Int2LongSortedMap mainQuestTitles = new Int2LongRBTreeMap(GameData.getMainQuestDataMap().int2ObjectEntrySet().stream().collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e ->  e.getValue().getTitleTextMapHash())));
        // Int2LongSortedMap questDescs = new Int2LongRBTreeMap(GameData.getQuestDataMap().int2ObjectEntrySet().stream().collect(Collectors.toMap(e ->  e.getIntKey(), e ->  e.getValue().getDescTextMapHash())));

//        final Int2LongSortedMap npcNames = new Int2LongRBTreeMap(GameData.getNpcDataMap().int2ObjectEntrySet().stream().collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e -> e.getValue().getNameTextMapHash())));
        final Int2LongSortedMap gadgetNames = new Int2LongRBTreeMap(GameData.getGadgetDataMap().int2ObjectEntrySet().stream().collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, e -> e.getValue().getNameTextMapHash())));

        // Preamble
        final List<StringBuilder> handbookBuilders = new ArrayList<>(TextStrings.NUM_LANGUAGES);
        final String now = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
        for (int i = 0; i < TextStrings.NUM_LANGUAGES; i++)
            handbookBuilders.add(new StringBuilder()
                .append("// Grasscutter " + GameConstants.VERSION + " GM Handbook\n")
                .append("// Created " + now + "\n\n")
                .append("// Commands\n"));
        // Commands
        final List<CommandHandler> cmdList = CommandMap.getInstance().getHandlersAsList();
        final String padCmdLabel = "%s:";
        for (CommandHandler cmd : cmdList) {
            final String label = padCmdLabel.formatted(cmd.getLabel());
            final String descKey = cmd.getDescriptionKey();
            for (int i = 0; i < TextStrings.NUM_LANGUAGES; i++) {
                String desc = languages.get(i).get(descKey).replace("\n", "\n\t");
                handbookBuilders.get(i).append(label + desc + "\n");
            }
        }
        // Avatars, Items, Monsters
        final ArrayList<String> handbookSections = new ArrayList<>(List.of("Avatars", "VirtualItems", "Weapons", "furniture", "Relics" /*, "Gadgets", "NPCs" */));
        final ArrayList<Int2LongSortedMap> handbookNames = new ArrayList<>(List.of(avatarNames, virtualItems, weaponItems, furnitureItems, relicNames /*, gadgetNames , npcNames */));
        handbookSections.addAll(Arrays.stream(materialTypes).map(Enum::name).toList());
        handbookNames.addAll(materialsNames);
        handbookSections.addAll(Arrays.stream(monsterTypes).map(Enum::name).toList());
        handbookNames.addAll(monsterNames);

        for (int section = 0; section < handbookSections.size(); section++) {
            final var h = handbookNames.get(section);
            final String s = "\n\n// " + handbookSections.get(section) + "\n";
            handbookBuilders.forEach(b -> b.append(s));
            final String padId = "%s:";
            h.forEach((id, hash) -> {
                final String sId = padId.formatted(id);
                final TextStrings t = textMaps.get( hash);
                for (int i = 0; i < TextStrings.NUM_LANGUAGES; i++)
                    handbookBuilders.get(i).append(sId + t.strings[i] + "\n");
            });
        }

        // Scenes - no translations
        var sceneTypes = SceneType.values();
        var sceneDataMap = GameData.getSceneDataMap();
        var scenes = Arrays.stream(sceneTypes).map(type ->
            new Int2ObjectRBTreeMap<>(
                sceneDataMap
                    .int2ObjectEntrySet()
                    .stream()
                    .filter(
                        e -> e.getValue().getSceneType() == type
                    ).collect(
                        Collectors.toMap(
                            Int2ObjectMap.Entry::getIntKey,
                            e -> e.getValue().getScriptData()
                        )
                    ))
        ).toList();

        handbookBuilders.forEach(b -> b.append("\n\n// Scenes\n"));
        int scenesTypeIndex = 0;
        for (var sceneType : sceneTypes) {
            for (var b : handbookBuilders) {
                b.append("\n\n// ").append(sceneType.name()).append('\n');
                for (var kv : scenes.get(scenesTypeIndex).int2ObjectEntrySet()) {
                    b.append(kv.getIntKey()).append(':').append(kv.getValue()).append('\n');
                }
            }
            scenesTypeIndex++;
        }

        // Dungeon - no translations
        handbookBuilders.forEach(b -> b.append("\n\n// Dungeons\n"));
        final var dungeonDataMap = GameData.getDungeonDataMap();
//        final String padDungeonId = "%d:";
        dungeonDataMap.keySet().intStream().sorted().forEach(id -> {
//            final String dId = padDungeonId.formatted(id);
            final int sceneId = dungeonDataMap.get(id).getSceneId();
//            final String sId = padDungeonId.formatted(sceneId);
            final String data = sceneDataMap.get(sceneId).getScriptData();
//            handbookBuilders.forEach(b -> b.append(dId).append(sId).append(data).append("\n"));
            handbookBuilders.forEach(b -> b.append(id).append(':').append(data).append("\n"));
        });



        // Quests
        handbookBuilders.forEach(b -> b.append("\n\n// Quests\n"));
        final var questDataMap = GameData.getQuestDataMap();
        final String padQuestId = "%d:";
        questDataMap.keySet().intStream().sorted().forEach(id -> {
            final String sId = padQuestId.formatted(id);
            final QuestData data = questDataMap.get(id);
            final var mainTitleId = mainQuestTitles.get(data.getMainId());
            if (mainTitleId != 0) {
                final TextStrings title = textMaps.get(mainTitleId);
                final TextStrings desc = textMaps.get( data.getDescTextMapHash());
                for (int i = 0; i < TextStrings.NUM_LANGUAGES; i++)
                    handbookBuilders.get(i).append(sId + title.strings[i] + " - " + desc.strings[i] + "\n");
            }
        });

        // Write txt files
        for (int i = 0; i < TextStrings.NUM_LANGUAGES; i++) {
            File GMHandbookOutputpath=new File("./GM Handbook");
            GMHandbookOutputpath.mkdir();
            final String fileName = "./GM Handbook/GM Handbook - %s.txt".formatted(TextStrings.ARR_LANGUAGES[i]);
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8), false)) {
                writer.write(handbookBuilders.get(i).toString());
            }
        }
        Grasscutter.getLogger().info("GM Handbooks generated!");
    }

    public static void createGachaMapping(String location) throws Exception {
        createGachaMappings(location);
    }

    public static List<String> createGachaMappingJsons() {
        final int NUM_LANGUAGES = Language.TextStrings.NUM_LANGUAGES;
        final Language.TextStrings CHARACTER = Language.getTextMapKey(4233146695L);  // "Character" in EN
        final Language.TextStrings WEAPON = Language.getTextMapKey(4231343903L);  // "Weapon" in EN
        final Language.TextStrings STANDARD_WISH = Language.getTextMapKey(332935371L);  // "Standard Wish" in EN
        final Language.TextStrings CHARACTER_EVENT_WISH = Language.getTextMapKey(2272170627L);  // "Character Event Wish" in EN
        final Language.TextStrings CHARACTER_EVENT_WISH_2 = Language.getTextMapKey(3352513147L);  // "Character Event Wish-2" in EN
        final Language.TextStrings WEAPON_EVENT_WISH = Language.getTextMapKey(2864268523L);  // "Weapon Event Wish" in EN
        final List<StringBuilder> sbs = new ArrayList<>(NUM_LANGUAGES);
        for (int langIdx = 0; langIdx < NUM_LANGUAGES; langIdx++)
            sbs.add(new StringBuilder("{\n"));  // Web requests should never need Windows line endings

        // Avatars
        GameData.getAvatarDataMap().keySet().intStream().sorted().forEach(id -> {
            AvatarData data = GameData.getAvatarDataMap().get(id);
            int avatarID = data.getId();
            if (avatarID >= 11000000) { // skip test avatar
                return;
            }
            String color = switch (data.getQualityType()) {
                case "QUALITY_PURPLE" -> "purple";
                case "QUALITY_ORANGE" -> "yellow";
                case "QUALITY_BLUE" -> "blue";
                default -> "";
            };
            Language.TextStrings avatarName = Language.getTextMapKey(data.getNameTextMapHash());
            for (int langIdx = 0; langIdx < NUM_LANGUAGES; langIdx++) {
                sbs.get(langIdx)
                    .append("\t\"")
                    .append(avatarID % 1000 + 1000)
                    .append("\": [\"")
                    .append(avatarName.get(langIdx))
                    .append(" (")
                    .append(CHARACTER.get(langIdx))
                    .append(")\", \"")
                    .append(color)
                    .append("\"],\n");
            }
        });

        // Weapons
        GameData.getItemDataMap().keySet().intStream().sorted().forEach(id -> {
            ItemData data = GameData.getItemDataMap().get(id);
            if (data.getId() <= 11101 || data.getId() >= 20000) {
                return; //skip non weapon items
            }
            String color = switch (data.getRankLevel()) {
                case 3 -> "blue";
                case 4 -> "purple";
                case 5 -> "yellow";
                default -> null;
            };
            if (color == null) return;  // skip unnecessary entries
            Language.TextStrings weaponName = Language.getTextMapKey(data.getNameTextMapHash());
            for (int langIdx = 0; langIdx < NUM_LANGUAGES; langIdx++) {
                sbs.get(langIdx)
                    .append("\t\"")
                    .append(data.getId())
                    .append("\": [\"")
                    .append(weaponName.get(langIdx).replaceAll("\"", "\\\\\""))
                    .append(" (")
                    .append(WEAPON.get(langIdx))
                    .append(")\", \"")
                    .append(color)
                    .append("\"],\n");
            }
        });

        for (int langIdx = 0; langIdx < NUM_LANGUAGES; langIdx++) {
            sbs.get(langIdx)
                .append("\t\"200\": \"")
                .append(STANDARD_WISH.get(langIdx))
                .append("\",\n\t\"301\": \"")
                .append(CHARACTER_EVENT_WISH.get(langIdx))
                .append("\",\n\t\"400\": \"")
                .append(CHARACTER_EVENT_WISH_2.get(langIdx))
                .append("\",\n\t\"302\": \"")
                .append(WEAPON_EVENT_WISH.get(langIdx))
                .append("\"\n}");
        }
        return sbs.stream().map(StringBuilder::toString).toList();
    }

    public static void createGachaMappings(String location) throws Exception {
        ResourceLoader.loadResources();
        List<String> jsons = createGachaMappingJsons();
        StringBuilder sb = new StringBuilder("mappings = {\n");
        for (int i = 0; i < Language.TextStrings.NUM_LANGUAGES; i++) {
            sb.append("\t\"%s\": ".formatted(Language.TextStrings.ARR_GC_LANGUAGES[i].toLowerCase()));  // TODO: change the templates to not use lowercased locale codes
            sb.append(jsons.get(i).replace("\n", "\n\t") + ",\n");
        }
        sb.setLength(sb.length() - 2);  // Delete trailing ",\n"
        sb.append("\n}");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(location), StandardCharsets.UTF_8), false)) {
            // if the user made choices for language, I assume it's okay to assign his/her selected language to "en-us"
            // since it's the fallback language and there will be no difference in the gacha record page.
            // The end-user can still modify the `gacha/mappings.js` directly to enable multilingual for the gacha record system.
            writer.println(sb);
            Grasscutter.getLogger().info("Mappings generated to " + location + " !");
        }
    }

    public static List<String> getAvailableLanguage() {
        List<String> availableLangList = new ArrayList<>();
        try {
            Files.newDirectoryStream(getResourcePath("TextMap"), "TextMap*.json").forEach(path -> {
                availableLangList.add(path.getFileName().toString().replace("TextMap", "").replace(".json", "").toLowerCase());
            });
        } catch (IOException e) {
            Grasscutter.getLogger().error("Failed to get available languages:", e);
        }
        return availableLangList;
    }

    @Deprecated(forRemoval = true, since = "1.2.3")
    public static String getLanguageOption() {
        List<String> availableLangList = getAvailableLanguage();

        // Use system out for better format
        if (availableLangList.size() == 1) {
            return availableLangList.get(0).toUpperCase();
        }
        StringBuilder stagedMessage = new StringBuilder();
        stagedMessage.append("The following languages mappings are available, please select one: [default: EN] \n");

        StringBuilder groupedLangList = new StringBuilder(">\t"); String input;
        int groupedLangCount = 0;

        for (String availableLanguage: availableLangList) {
            groupedLangCount++;
            groupedLangList.append(availableLanguage).append("\t");

            if (groupedLangCount == 6) {
                stagedMessage.append(groupedLangList).append("\n");
                groupedLangCount = 0;
                groupedLangList = new StringBuilder(">\t");
            }
        }

        if (groupedLangCount > 0) {
            stagedMessage.append(groupedLangList).append("\n");
        }

        stagedMessage.append("\nYour choice: [EN] ");

        input = Grasscutter.getConsole().readLine(stagedMessage.toString());
        if (availableLangList.contains(input.toLowerCase())) {
            return input.toUpperCase();
        }

        Grasscutter.getLogger().info("Invalid option. Will use EN (English) as fallback."); return "EN";
    }
}