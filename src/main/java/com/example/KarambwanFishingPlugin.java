package net.runelite.client.plugins.karambwanfishing;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Karambwan Fishing",
	description = "Swap Last Destination/Configure options on Zanaris Fairy Ring for easy Karambwan (CKR) teleport after banking."
)
public class KarambwanFishingPlugin extends Plugin
{
	private final int FAIRY_RING_ID_ZANARIS = 29560;

	private final String MENU_OPTION_CONFIGURE = "configure";
	private final String MENU_OPTION_LAST_DEST = "last-destination";

	@Inject
	private Client client;

	@Subscribe(priority = 10)
	public void onClientTick(ClientTick clientTick) {
		/**
		 * Do nothing if no player is logged in OR the menu is open. Don't want to rebuild
		 * the menu while it's open.
		 */
		if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen()) {
			return;
		}

		// Get the selected tile (tile at mouse)
		Tile selectedTile =  client.getSelectedSceneTile();

		// Check if the selected tile has the Zanaris fairy ring on it
		boolean zanarisRingTile = tileHasZanarisFairyRing(selectedTile);

		if(zanarisRingTile == true) {
			swapMenuEntries();
		}
	}

	/**
	 * Check if the the tile has the Zanaris Fairy Ring in list of game objects.
	 *
	 * @param tile - tile to check for zanaris fairy ring
	 * @return boolean
	 */
	private boolean tileHasZanarisFairyRing(Tile tile) {
		if(tile == null || tile.getGameObjects() == null) {
			return false;
		}

		GameObject[] tileGameObjs = tile.getGameObjects();
		for(GameObject obj : tileGameObjs) {
			if(obj != null && obj.getId() == FAIRY_RING_ID_ZANARIS) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check the current menu entries for "Configure" and "Last Destination" options.
	 * Swap their positions in the menu entries if they are both found.
	 */
	private void swapMenuEntries() {
		MenuEntry[] menuEntries = client.getMenuEntries();

		int findIndex = 0;
		int configureIndex = -1;
		int lastDestinationIndex = -1;

		// Find indices for configure and last destination options
		for(MenuEntry menuEntry : menuEntries) {
			String option = Text.removeTags(menuEntry.getOption()).toLowerCase();

			if(option.equals(MENU_OPTION_CONFIGURE)) {
				configureIndex = findIndex;
			} else if(option.startsWith(MENU_OPTION_LAST_DEST)) {
				lastDestinationIndex = findIndex;
			}

			findIndex++;
		}

		if(configureIndex != -1 && lastDestinationIndex != -1) {
			// We found the indices of both options, swap them
			MenuEntry tempEntry = menuEntries[configureIndex];
			menuEntries[configureIndex] = menuEntries[lastDestinationIndex];
			menuEntries[lastDestinationIndex] = tempEntry;
		}

		// Set the client menu entries with new swapped options
		client.setMenuEntries(menuEntries);
	}
}
