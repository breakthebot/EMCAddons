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

import org.breakthebot.EMCAddons.EMCAddons;
import org.breakthebot.EMCAddons.hideNSeek.gameListeners;
import org.breakthebot.EMCAddons.hideNSeek.hideNSeek;

import java.util.ArrayList;
import java.util.List;

public class manager {
    public static List<String> currentEvents = new ArrayList<>();
    private hideNSeek current;

    private static manager instance;
    public static manager getInstance() {
        if (instance == null) { instance = new manager(); }
        return instance;
    }

    public void setCurrent(hideNSeek current) {
        currentEvents.add(current.getEventName());
        this.current = current;
    }

    public hideNSeek getCurrent() {
        return this.current;
    }

    public void endCurrent() {
        currentEvents.remove(current.getEventName());
        EMCAddons.getInstance().eventUnregister(current.getListenerInstance());
        gameListeners.clearPending();
        this.current = null;
    }
}
