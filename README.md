## EMC Addons - Simple modifications for EMC  
### Vanish function  
- Upon entering spectator mode, players are hidden from all other online players  
**Required permission:** `ly.skynet.specvanish`
- Users with the permission `ly.skynet.see.specvanish` can see players in vanish regardless.

### EventManager
Main command: `/eventmanager` [aliases: `/em`, `/event`]
### Permissions:  
- eventmanager.admin:  
Grant all eventmanager permissions  

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