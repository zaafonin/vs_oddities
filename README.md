<p align="center">
<img src="fabric/src/main/resources/logo.png" width="200" height="200">
</p>

# VS Oddities
The wacky wonky little brother of [VS Addition by xiewuzhiying.](https://github.com/xiewuzhiying/VS-Addition) Intended to complement [Valkyrien Skies 2](https://github.com/ValkyrienSkies/Valkyrien-Skies-2) with minor bug fixes and integration with vanilla and some modded mechanics.

## Disclaimer:
The addon is still in its early development and has several known and most probably some unknown bugs. I will provide .jar files once I consider the mod stable enough to play it with existing worlds.

## Features:
- Various bug fixes:
- - Newly created ships retain the biome they were created in. No more mismatch of grass colors!
- - ~~World weather applies to ships correctly. Snow's falling around? It will pile up on your ship as well.~~ Implemented in VS2 now.
- - Lighting on ships accounts for lights in the world around them. Ships parked in dark areas will be dark as well.
- - ~~Vanilla sculk now works both in world and on ships. This also applies to [Echo Chest](https://github.com/Fuzss/echochest), helping you to collect items around you while being on a ship.~~ Merged into VS2.
- - ~~Better handling of entities tied to ships (minecarts, leashes, etc.)~~ Merged into VS2, aside from minecarts snapping to ship rails.
- - Sensible default masses for blocks without a defined weight; no more 1000 kg slabs, panes and trapdoors.
- More integration with [Create](https://github.com/Creators-of-Create/Create):
- - Blaze burners, basins, mixers interact with each other even on different ships.
- - Blowing air on ships using encased fans propels them.
- - Ships spin on top of spinning turntables.
- Custom features:
- - When [HT's Treechop](https://modrinth.com/mod/treechop) is installed, felling a tree "shipifies" it. This is useful for bringing a new VS2 ship into existence without any helms, wands and assemblers.
- - Fireworks and other projectiles add noticeable recoil when shot from dispensers. This allows for a makeshift thruster using only vanilla blocks and items.
- - Ships can be dragged around by mobs leashed to them.
- - Some blocks have a mysterious property of negating gravity. Build floating islands without worrying about thrust!
- Quality of life for other VS2 tinkerers:
- - ~~Info about VS2 ships (mass, velocity, attachments, etc.) all in the F3 menu.~~ Now mostly in base VS2. Attachment display might be reworked
- - Two new commands:
- - - `/vs break` destroys all blocks of a ship as if they were broken naturally (enabling drops)
- - - `/vs dry` removes all water of a ship, including kelp and seagrass. Waterlogged blocks are also handled correctly.


The feature set will be expanded and polished. If you need compat with a specific mod, open a GitHub issue or contact me on the [Valkyrien Skies Discord](https://discord.com/invite/dWwM8G3), you'll find me as @zaafonin. I'll try to come up with some solution if it's feasible for me.

## Contributions:
VS Oddities is mostly an assortment of small mixins barely connected to each other, so if you've made a patch for a mod it should be easy to accept it. If you depend on some new mod, make sure to include its gradle import in your pull request.

## Licensing:
I use the MIT license as I welcome other, probably more competent developers to include and rework my code into their own addons. Credits are welcome but not necessary.

Due to the nature of my addon (mixins for vanilla and other mods, including VS2 itself) I may have overlooked the licensing restrictions of your mod. If you believe your copyright has been infringed, contact me via zakharafoniam@gmail.com and we will discuss how to handle this problem.
