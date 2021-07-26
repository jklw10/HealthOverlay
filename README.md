# Health-Overlay
Replacement for the default heart renderer with various configuration options and features

CurseForge webpage containing downloads: https://www.curseforge.com/minecraft/mc-mods/health-overlay

### Config Values

```
[health]
    # Colors for every 10 hearts (not counting the default red)
    # All values are written as hexadecimal RGB color in '#RRGGBB' format
    healthColors = ["#FFFFFF", "...", ...]
    
    # Show vanilla hearts
    healthVanilla = true
    
    # Two alternating colors when poisoned
    healthPoisonColors = ["...", "..."]

    # Two alternating colors when withered
    healthWitherColors = ["...", "..."]

[absorption]
    # Colors for every 10 hearts (not counting the default yellow)
    # All values are written as hexadecimal RGB color in '#RRGGBB' format
    absorptionColors = ["...", "...", ...]
    
    # Show vanilla hearts
    absorptionVanilla = true
    
    # Two alternating colors when poisoned
    # Can be empty in case of vanilla behaviour where heart background is rendered without hearts
    absorptionPoisonColors = []
    
    # Two alternating colors when withered
    # Can be empty in case of vanilla behaviour where heart background is rendered without hearts
    absorptionWitherColors = []
    
    [absorption.advanced]
        # Display absorption in the same row as health
        # Absorption is rendered after and over health depending on the mode
        absorptionOverHealth = false
        
        # Display mode for absorption
        # absorption.advanced.absorptionOverHealth must to be true
        # Modes: 
        #   "BEGINNING":
        #     Absorption always starts at first heart.
        #   "AFTER_HEALTH":
        #     Absorption starts after the last highest health heart and loops back to first health heart if overflowing.
        #     This means that health hearts will be hidden when absorption has 10 or more hearts.
        #       Example 1: If a player has 10 health (5 hearts), absorption will render itself in the last
        #                    five hearts and in case it is higher it will loop back over first five health hearts.
        #       Example 2: If a player has more than 20 absorption, second color is shown the same way as in "BEGINNING".
        #       Example 3: If player health is divisible by 20, absorption is shown the same way as in "BEGINNING".
        #   "AFTER_HEALTH_ADVANCED":
        #     Absorption starts after the last highest health heart and loops back to first absorption heart if overflowing.
        #     This means that no matter how much absorption there is, health hearts will almost always be visible.
        #       Example 1: If a player has 18 health (9 hearts), absorption will render itself in the last
        #                    empty heart and color itself accordingly, e.g. absorption 0 has 2 hearts and
        #                    will render using the second color as the first color is used for the first heart.
        #       Example 2: If a player has 30 health (15 hearts), absorption will render itself in the last
        #                    five hearts and color itself accordingly, e.g. absorption 2 has 6 hearts and
        #                    will render first heart using second color and rest using first color.
        #       Example 3: If player health is divisible by 20, absorption is shown the same way as in "BEGINNING".
        #   "AS_HEALTH":
        #     Absorption is rendered as health, making all colors and values same as health
        absorptionOverHealthMode = "AFTER_HEALTH_ADVANCED"

```

### Textures
Visible in `src/main/resources/assets/healthoverlay/textures`

###### half_heart.png
- Self-explanatory half heart backgrounds
- Used for half heart absorptions or when max health is lowered below 20
- Dark value is the standard and white value is displayed when health is regenerating

###### absorption.png
- Absorption hearts colored via config values
- First row of hearts is used in non-hardcore worlds and second row in hardcore worlds
  - From left to right: standard, poison, wither
  - The row right below is used to add an accent to the heart
    - Non-hardcore accent is rendered with no transparency
    - Hardcore accent is rendered with '88/255' RGBA transparency  
      expect it to look a lot paler to what it is in file
      
###### health.png
- Health hearts colored via config values
- First row of hearts is used in non-hardcore worlds and second row in hardcore worlds
  - From left to right: standard, poison, wither
  - First row below is used to add an accent to the heart
    - Non-hardcore accent is rendered with no transparency
    - Hardcore accent is rendered with '178/255' RGBA transparency  
      expect it to look a little paler to what it is in file
  - Second row below is used to add shading to the bottom of the heart
    - Rendered with '56/255' RGBA transparency  
      expect it to look a lot paler to what it is in file
  - Third row below is used to add shading when withered around the heart
    - Rendered with no transparency