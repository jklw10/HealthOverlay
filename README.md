# Health-Overlay
Replacement for the default heart renderer with various configuration options and features

CurseForge webpage containing downloads:  
Fabric: https://www.curseforge.com/minecraft/mc-mods/health-overlay-fabric  
Forge: https://www.curseforge.com/minecraft/mc-mods/health-overlay

### Config Values

```
{
	"health": {
		// Show vanilla hearts
		"health_vanilla": true,
		/* Colors for every 10 hearts (not counting the default red)
		   All values are written as hexadecimal RGB color in '#RRGGBB' format
		*/
		"health_colors": [
			"...",
			"...",
			"..."
		],
		/* Two alternating colors when poisoned
		   There can be one color in case vanilla poisoned heart is wanted
		*/
		"health_poison_colors": [
			"...",
			"..."
		],
		/* Two alternating colors when withered
		   There can be one color in case vanilla withered heart is wanted
		*/
		"health_wither_colors": [
			"...",
			"..."
		],
		/* Two alternating colors when freezing
		   There can be one color in case vanilla frozen heart is wanted
		*/
		"health_frozen_colors": [
			"...",
			"..."
		]
	},
	"absorption": {
		// Show vanilla hearts
		"absorption_vanilla": true,
		/* Colors for every 10 hearts (not counting the default yellow)
		   All values are written as hexadecimal RGB color in '#RRGGBB' format
		*/
		"absorption_colors": [
			"...",
			"...",
			"..."
		],
		// Two alternating colors when poisoned
		"absorption_poison_colors": [
			"...",
			"..."
		],
		// Two alternating colors when withered
		"absorption_wither_colors": [
			"...",
			"..."
		],
		// Two alternating colors when freezing
		"absorption_frozen_colors": [
			"...",
			"..."
		],
		"advanced": {
			/* Display absorption in the same row as health
			   Absorption is rendered after and over health depending on the mode
			*/
			"absorption_over_health": false,
			/* Display mode for absorption
			   absorption.advanced.absorptionOverHealth must to be true
			   Modes:
			     "BEGINNING":
			       Absorption always starts at first heart.
			     "AFTER_HEALTH":
			       Absorption starts after the last highest health heart and loops back to first health heart if overflowing.
			       This means that health hearts will be hidden when absorption has 10 or more hearts.
			         Example 1: If a player has 10 health (5 hearts), absorption will render itself in the last
			                      five hearts and in case it is higher it will loop back over first five health hearts.
			         Example 2: If a player has more than 20 absorption, absorption is shown the same way as in "BEGINNING".
			*/
			"absorption_over_health_mode": "AFTER_HEALTH"
		}
	}
}
```

### Textures
Visible in `src/main/resources/assets/healthoverlay/textures`

###### half_heart.png
- Self-explanatory half heart backgrounds
- Used for half heart absorptions or when max health is lowered below 20
- Dark value is the standard and white value is displayed when health is regenerating

###### absorption.png & health.png
- Absorption and health hearts colored via config values
- First row of hearts is used in non-hardcore worlds and second row in hardcore worlds
    - From left to right: standard, poison, wither, frozen
    - First row below hearts is used to add an accent
      - Non-hardcore is rendered with 216/255 RGBA transparency
      - Hardcore is rendered with '88/255' (absorption) & '178/255' (health) RGBA transparency  
    - Second row below hearts is used to add shading
      - Rendered with '56/255' RGBA transparency, when withered '216/255'