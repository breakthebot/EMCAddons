package org.breakthebot.EMCAddons.events;

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


import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private static final List<BaseEvent> currentEvents = new ArrayList<>();

    public static List<String> getCurrentEventNames() {
        List<String> names = new ArrayList<>();
        for (BaseEvent event : getCurrentEvents()) {
            names.add(event.getEventName());
        }
        return names;
    }

    public static List<BaseEvent> getCurrentEvents() { return currentEvents; }

    public static void addEvent(BaseEvent event) { currentEvents.add(event); }
    public static void removeEvent(BaseEvent event) { currentEvents.remove(event); }
}
