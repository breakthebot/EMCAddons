package org.breakthebot.EMCAddons.events.tag;

/*
 * This file is part of EMCAddons.
 *
 * EMCAddons is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EMCAddons is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EMCAddons. If not, see <https://www.gnu.org/licenses/>.
 */

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.breakthebot.EMCAddons.EMCAddons;
import org.breakthebot.EMCAddons.events.MainUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TagUtils {

    public static ScheduledTask scheduleDisqualify(UUID uuid , int duration) {
        EMCAddons instance = EMCAddons.getInstance();
        return instance.getServer().getAsyncScheduler().runDelayed(instance, task ->   {
            Tag current = Tag.getInstance();
            if (current == null) return;
            if (TagListeners.disqualifyTasks.remove(uuid) == null) return;
            instance.getServer().getScheduler().runTask(instance, () -> TagListeners.disqualifyPlayer(uuid));
        }, duration, TimeUnit.SECONDS);
    }

    public static boolean isPlayer(Player player) {
        Tag current = Tag.getInstance();
        if (current == null) return false;
        return current.getPlayers().contains(player);
    }

    public static boolean isDisqualified(Player player) {
        Tag current = Tag.getInstance();
        if (current == null) return false;
        return current.getDisqualified().contains(player);
    }

    public static void setGlow(Player player) {
        player.setGlowing(true);
    }

    public static void removeGlow(Player player) {
        player.setGlowing(false);
    }

    public static void handleTags() {
        Tag current = Tag.getInstance();
        if (current == null) return;

        int totalPlayers = current.getPlayers().size();
        int currentTagged = current.getTagged().size();
        int supposedTagged = (int) Math.max(Math.ceil(totalPlayers * 0.05), 1);
        if (currentTagged > supposedTagged) {
            List<Player> tagged = current.getPlayers().stream()
                    .filter(current::isTagged)
                    .collect(Collectors.toList());

            if (tagged.isEmpty()) return;
            Collections.shuffle(tagged);
            Player player = tagged.getFirst();

            current.removeTagged(player);
            TagUtils.removeGlow(player);
            player.sendMessage(Component.text("You're no longer tagged due to a lower play count.")
                    .color(NamedTextColor.RED));

        }
        else if (currentTagged < supposedTagged){
            List<Player> untagged = current.getPlayers().stream()
                    .filter(p -> !current.isTagged(p))
                    .collect(Collectors.toList());

            if (untagged.isEmpty()) return;

            Collections.shuffle(untagged);
            Player player = untagged.getFirst();

            current.addTagged(player);
            TagUtils.setGlow(player);
            player.sendMessage(Component.text("You're IT. Tag others to lose it.")
                    .color(NamedTextColor.RED));
            current.broadcastPlayers(player.getName() + " is now Tagged!");
        }
    }


    public static boolean allowTabComplete(CommandSender sender) {
        return sender.hasPermission("eventmanager.tag") ||
                sender.hasPermission("eventmanager.tag.event.create") ||
                sender.hasPermission("eventmanager.tag.event.start") ||
                sender.hasPermission("eventmanager.tag.event.end") ||
                sender.hasPermission("eventmanager.tag.manage.player") ||
                sender.hasPermission("eventmanager.tag.status");
    }

    public static void sendSummary(Tag instance) {
        List<Player> remainingPlayers = instance.getPlayers();
        List<Player> disqualifiedPlayers = instance.getDisqualified();
        List<Player> taggedPlayers = instance.getTagged();

        int remaining = remainingPlayers.size();
        int disqualified = disqualifiedPlayers.size();
        int participants = remaining + disqualified;
        int tagged = taggedPlayers.size();

        Component standingHover = getHover("Standing Players:\n", remainingPlayers);

        Component disqualifiedHover = getHover("Disqualified Players:\n", disqualifiedPlayers);

        Component taggedHover = getHover("Tagged:\n", taggedPlayers);

        Component summary = Component.text("[Event Broadcast] ")
                .color(NamedTextColor.BLUE)
                .append(Component.text("The Tag event has come to an end!\n")
                        .color(NamedTextColor.GREEN))
                .append(Component.text(remaining + " players stand!\n")
                        .color(NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(standingHover))
                )
                .append(Component.text(disqualified + " players have been disqualified.\n")
                        .color(NamedTextColor.DARK_RED)
                        .hoverEvent(HoverEvent.showText(disqualifiedHover))
                )
                .append(Component.text(participants + " total participants, " + tagged + " total tagged.\n")
                        .color(NamedTextColor.GRAY)
                        .hoverEvent(HoverEvent.showText(taggedHover))
                )
                .append(Component.text("Thank you all for attending!")
                        .color(NamedTextColor.GOLD)
                );

        MainUtils.broadcastGlobal(summary);
    }

    private static @NotNull Component getHover(String content, List<Player> players) {
        Component standingHover = Component.text(content);

        if (players.isEmpty()) {
            standingHover = standingHover.append(Component.text("\n- None"));
        } else {
            for (Player player : players) {
                standingHover = standingHover
                        .append(Component.text("\n- " + player.getName()));
            }
        }

        return standingHover;
    }
}
