/*
    This file is part of the McOverTor project, licensed under the
    GNU General Public License v3.0

    Copyright (C) 2024-2026 _1ms (GRX005)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package _1ms.McOverTor.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static _1ms.McOverTor.Main.confPath;

public class RegionMgr {
    public record TorRegionInfo(String code, String name) {}
    private static final Path torrc = confPath.resolve("torrc");
    private static final Logger logger = LogManager.getLogger("McOverTor/Regions");
//Read all possible countries from the Tor client's geopip db file, and create a record with it's code and full name.
    public static List<TorRegionInfo> getCtr() {
        try (Stream<String> stream = Files.lines(confPath.resolve("geoip"))) {
            return stream
                    .filter(c->!c.startsWith("#") && !c.endsWith("?") && !c.isEmpty())
                    .map(l->l.substring(l.lastIndexOf(",") + 1))//Filter out ctr codes
                    .distinct()
                    .map(c->new TorRegionInfo(c, new Locale.Builder().setRegion(c).build().getDisplayCountry()))
                    .filter(c->c.name.length() > 2)//remove those codes which java can't find a name for.
                    .sorted(Comparator.comparing(TorRegionInfo::name))
                    .toList();
        } catch (IOException e) {
            logger.error("Failed to get countries from tor's db.");
            throw new RuntimeException(e);
        }
    }

//Get the selected countries from the Tor client's torrc config file. NOTE: HashSet, bc in its used in a render loop with .contains()
    public static Set<String> getSelCtr() {
        try {
            String ln = Files.readAllLines(torrc).getLast().substring(10);
            Set<String> codes = new HashSet<>();
            Matcher mtr = Pattern.compile("\\{([A-Z]{2})}").matcher(ln);
            while (mtr.find())
                codes.add(mtr.group(1));
            return codes;
        } catch (IOException e) {
            logger.error("Failed to get selected countries");
            throw new RuntimeException(e);
        }
    }
//Apply the modifed regions as needed to the torrc file.
    public static void modRegions(Set<String> regs) {
        try {
            List<String> lines = Files.readAllLines(torrc);
            final String lastLn = lines.getLast();
            if(regs.isEmpty()) { //IF none are selected, delete
                lines.remove(lastLn);
                if(lines.getLast().startsWith("MiddleNodes"))
                    for(int i=0; i<2; i++)
                        lines.remove(lines.getLast());
            } else {
                StringBuilder toSave = new StringBuilder();
                regs.forEach(e-> toSave.append("{").append(e).append("},"));
                if (lastLn.startsWith("ExitNodes")) { //IF it was already there we need to delete the previous.
                    lines.remove(lastLn); //We have a new last here.
                    if(lines.getLast().startsWith("MiddleNodes"))
                        for(int i=0; i<2; i++)
                            lines.remove(lines.getLast());
                }
                if(SettingsMgr.get(TorOption.allNodes)) {//Also entry and middle if we want it to apply to all nodes.
                    lines.add("EntryNodes "+ toSave);
                    lines.add("MiddleNodes "+ toSave);
                }
                lines.add("ExitNodes "+ toSave);
            }
            Files.write(torrc, lines);
        } catch (IOException e) {
            logger.error("Failed to apply country modifications");
            throw new RuntimeException(e);
        }
    }
//If only the TorOption.allNodes option changed, not the selection of countries
    public static void remOrAdd(boolean add) {
        try {
            List<String> lines = Files.readAllLines(torrc);
            if(!add) {
                for(int i=0; i<2; i++)
                    lines.remove(lines.size()-2);
            } else {
                final String toAdd = lines.getLast().substring(10);
                lines.add(lines.size()-1,"EntryNodes "+toAdd);
                lines.add(lines.size()-1,"MiddleNodes "+toAdd);
            }
            Files.write(torrc, lines);
        } catch (IOException e) {
            logger.error("Failed to change single/multi nodes application.");
            throw new RuntimeException(e);
        }
    }

}
