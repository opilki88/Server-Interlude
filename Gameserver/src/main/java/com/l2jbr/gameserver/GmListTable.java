/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jbr.gameserver;

import com.l2jbr.commons.Config;
import com.l2jbr.gameserver.model.actor.instance.L2PcInstance;
import com.l2jbr.gameserver.network.SystemMessageId;
import com.l2jbr.gameserver.serverpackets.L2GameServerPacket;
import com.l2jbr.gameserver.serverpackets.SystemMessage;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class stores references to all online game masters. (access level > 100)
 *
 * @version $Revision: 1.2.2.1.2.7 $ $Date: 2005/04/05 19:41:24 $
 */
public class GmListTable {
    private static Logger _log = Logger.getLogger(GmListTable.class.getName());
    private static GmListTable _instance;

    /**
     * Set(L2PcInstance>) containing all the GM in game
     */
    private final Map<L2PcInstance, Boolean> _gmList;

    public static GmListTable getInstance() {
        if (_instance == null) {
            _instance = new GmListTable();
        }
        return _instance;
    }

    public List<L2PcInstance> getAllGms(boolean includeHidden) {
        List<L2PcInstance> tmpGmList = new LinkedList<>();

        for (Map.Entry<L2PcInstance, Boolean> n : _gmList.entrySet()) {
            if (includeHidden || !n.getValue()) {
                tmpGmList.add(n.getKey());
            }
        }

        return tmpGmList;
    }

    public List<String> getAllGmNames(boolean includeHidden) {
        List<String> tmpGmList = new LinkedList<>();

        for (Map.Entry<L2PcInstance, Boolean> n : _gmList.entrySet()) {
            if (!n.getValue()) {
                tmpGmList.add(n.getKey().getName());
            } else if (includeHidden) {
                tmpGmList.add(n.getKey().getName() + " (invis)");
            }
        }

        return tmpGmList;
    }

    private GmListTable() {
        _gmList = new LinkedHashMap<>();
    }

    /**
     * Add a L2PcInstance player to the Set _gmList
     *
     * @param player
     * @param hidden
     */
    public void addGm(L2PcInstance player, boolean hidden) {
        if (Config.DEBUG) {
            _log.fine("added gm: " + player.getName());
        }
        _gmList.put(player, hidden);
    }

    public void deleteGm(L2PcInstance player) {
        if (Config.DEBUG) {
            _log.fine("deleted gm: " + player.getName());
        }

        _gmList.remove(player);
    }

    /**
     * GM will be displayed on clients gmlist
     *
     * @param player
     */
    public void showGm(L2PcInstance player) {
        for(L2PcInstance pcInstance : _gmList.keySet()) {
            if(pcInstance.equals(player)) {
                _gmList.put(pcInstance, false);
            }
        }
    }

    /**
     * GM will no longer be displayed on clients gmlist
     *
     * @param player
     */
    public void hideGm(L2PcInstance player) {
        for (L2PcInstance pcInstance : _gmList.keySet()) {
            if(pcInstance.equals(player)) {
                _gmList.put(pcInstance, true);
            }
        }
    }

    public boolean isGmOnline(boolean includeHidden) {
        for (Boolean value : _gmList.values()) {
            if(includeHidden || !value) {
                return  true;
            }
        }
        return false;
    }

    public void sendListToPlayer(L2PcInstance player) {
        if (!isGmOnline(player.isGM())) {
            SystemMessage sm = new SystemMessage(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW); // There are not any GMs that are providing customer service currently.
            player.sendPacket(sm);
        } else {
            SystemMessage sm = new SystemMessage(SystemMessageId.GM_LIST);
            player.sendPacket(sm);

            for (String name : getAllGmNames(player.isGM())) {
                sm = new SystemMessage(SystemMessageId.GM_S1);
                sm.addString(name);
                player.sendPacket(sm);
            }
        }
    }

    public static void broadcastToGMs(L2GameServerPacket packet) {
        for (L2PcInstance gm : getInstance().getAllGms(true)) {
            gm.sendPacket(packet);
        }
    }

    public static void broadcastMessageToGMs(String message) {
        for (L2PcInstance gm : getInstance().getAllGms(true)) {
            gm.sendPacket(SystemMessage.sendString(message));
        }
    }
}