## EMC Addons - Simple modifications for EMC  
### Vanish function  
- Upon entering spectator mode, players are hidden from all other online players  
**Required permission:** `ly.skynet.specvanish`
- Users with the permission `ly.skynet.see.specvanish` can see players in vanish regardless.

### Hide & Seek EventManager
Command: /eventmanager [alias: /em]  
Permissions:  
- eventmanager.admin: Grant all eventmanager permissions  
Specific permissions:
- eventmanager.event.start: Grant usage to /em start  
Start an event
- eventmanager.event.end: Grant usage to /em end  
End the current event
- eventmanager.manage.players: Grant usage to /em player add|remove|list|disqualify  
Register, remove, list, or disqualify participating players
- eventmanager.manage.hunters: Grant usage to /em hunter add|remove|list  
Register, remove, or list hunters (seekers)
- eventmanager.broadcast: Grant usage to /em broadcast {msg}  
Broadcast a global message on behalf of the plugin
- eventmanager.giveall: Grant usage to /em giveall player|hunter  
Give all players/hunters a clone of the item in your mainhand