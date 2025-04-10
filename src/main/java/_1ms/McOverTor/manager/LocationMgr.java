package _1ms.McOverTor.manager;

import _1ms.McOverTor.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static _1ms.McOverTor.Main.confPath;

public class LocationMgr {
    public record TorRegionInfo(String code, String name) {}

    public static List<TorRegionInfo> getCtr() {
        try (Stream<String> stream = Files.lines(Path.of(Main.confPath + "/geoip"))) {
            return stream
                    .map(LocationMgr::checker)
                    .filter(c->!c.isEmpty())
                    .distinct()
                    .map(c->new TorRegionInfo(c, new Locale.Builder().setRegion(c).build().getDisplayCountry()))
                    .sorted(Comparator.comparing(TorRegionInfo::name))
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<String> getSelCtr() {
        try {
            String ln = Files.readAllLines(Path.of(Main.confPath + "/torrc")).getLast().substring(10);
            Set<String> codes = new HashSet<>();
            Matcher mtr = Pattern.compile("\\{([A-Z]{2})}").matcher(ln);
            while (mtr.find())
                codes.add(mtr.group(1));
            return codes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String checker(String val) {
        if(val.contains("#"))
            return "";
        while (val.startsWith(",") || val.startsWith("?") || val.chars().anyMatch(Character::isDigit))
            val = val.substring(1);
        return val;
    }

    public static void modRegions(Set<String> regs) {
        Path file = Path.of(confPath+"/torrc");
        try {
            List<String> lines = Files.readAllLines(file);
            if(regs.isEmpty()) { //IF it's empty.
                lines.remove(lines.getLast());
            } else {
                StringBuilder toSave = new StringBuilder();
                regs.forEach(e-> toSave.append("{").append(e).append("},"));
                if (lines.getLast().startsWith("ExitNodes")) //IF it was already there we need to delete the previous.
                    lines.remove(lines.getLast());
                lines.add("ExitNodes "+ toSave);
            }
            Files.write(file, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
