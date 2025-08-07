## EMC Addons - Simple modifications for EMC  
### Vanish function  
- Upon entering spectator mode, players are hidden from all other online players  
**Required permission:** `ly.skynet.specvanish`
- Users with the permission `ly.skynet.see.specvanish` can see players in vanish regardless.

### Hide & Seek EventManager
Main command: `/eventmanager` [aliases: `/em`, `/event`]
### Permissions & subcommands:  
- eventmanager.admin:  
Grant all eventmanager permissions  

Specific permissions:
- eventmanager.event.start:  
Grant usage to **`/em start`** - Start an event
- eventmanager.event.end:  
Grant usage to **`/em end`** - End the current event
- eventmanager.manage.players:  
Grant usage to **`/em player add|remove|list|disqualify`**  
Register, remove, list, or disqualify participating players
- eventmanager.manage.hunters:  
Grant usage to **`/em hunter add|remove|list`**  
Register, remove, or list hunters (seekers)
- eventmanager.broadcast:  
Grant usage to **`/em broadcast {msg}`**  
Broadcast a global message with the prefix "[Event Broadcast]"
- eventmanager.giveall:  
Grant usage to **`/em giveall player|hunter`**  
Give all players/hunters a clone of the item in your mainhand


### Listeners
- PvP  
  Only hunters can attack others.
- Logoff  
  Add to pending disqualification. In 60 seconds, server will check if the player still has a pending removal, if so they will be disqualified  
  Only way to be removed from the pending list is to log back in before the 60 seconds pass
- Login  
  As mentioned above, cancel pending disqualification for player  
  Also notifies if this is the 2nd login of the player within the past 5 minutes
- Death  
  Strike lightning at death location  
  keepInv for gold (nuggets, ingots, and blocks)
- Respawn  
  return the gold mentioned above to the player
- TP away  
  Cancel any attempts to teleport away using Towny, if the current location is the event town
- Moving across chunks  
  Disqualify players if they attempt to leave the event town's claims
- Ender chest  
  Cancel opening enderchests