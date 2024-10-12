package undobutton.compatibility;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.google.gson.JsonElement;
import undobutton.util.MakeUndoable;

@MakeUndoable(statetype = JsonElement.class)
public class RelicStatsCompatibility {
    public static JsonElement save() {
        if (checkForMod()) {
            try {
                return (JsonElement) Class.forName("relicstats.StatsSaver").getMethod("saveRelics").invoke(null);
            } catch (Exception e) {
                // Should not happen.
                throw new RuntimeException("Failed to save state for RelicStats", e);
            }
        }
        return null;
    }

    public static void load(JsonElement element) {
        if (checkForMod()) {
            try {
                Class.forName("relicstats.StatsSaver").getMethod("loadRelics", JsonElement.class).invoke(null, element);
            } catch (Exception e) {
                // Should not happen.
                throw new RuntimeException("Failed to load state for RelicStats", e);
            }
        }
    }

    private static boolean checkForMod() {
        return Loader.isModLoaded("RelicStats");
    }
}
