    package me.lakoba.pureSelling;

    import me.clip.placeholderapi.expansion.PlaceholderExpansion;
    import org.bukkit.entity.Player;
    import org.jetbrains.annotations.NotNull;

    public class PlaceHolderApiHandler extends PlaceholderExpansion {

        private final PureSelling plugin;

        public PlaceHolderApiHandler(PureSelling plugin) {
            this.plugin = plugin;
        }

        @Override
        public @NotNull String getAuthor() {
            return "Lakoba";
        }

        @Override
        public @NotNull String getIdentifier() {
            return "pureselling";
        }

        @Override
        public @NotNull String getVersion() {
            return "0.1-ALPHA";
        }

        @Override
        public boolean canRegister() {
            return true;
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(Player p, String identifier) {
            if (identifier.startsWith("time")) {
                // Převod času v sekundách na minutový a sekundový formát
                int minutes = plugin.time / 60;  // Počet minut
                int seconds = plugin.time % 60;  // Počet sekund

                // Formátování času do formátu MM:SS
                return String.format("%02d:%02d", minutes, seconds);
            }

            return "Žádná hodnota";
        }

    }