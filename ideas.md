# series of chubes

### _it's not a dump truck_

Brainstorm for an advanced item/fluid/power for GregTech that builds off of GT's pipes, covers, and cables, but offers logistics more in line with what more powerful transport mods provide (eg. eio conduits, SFM, laserIO, Xnet)

### goals

* GregTech packs would rather use Chubes than something like `eio` or `laserIO` as it integrates better with GT.
* Players get flexible, powerful logistics without having to use `applied-energistics-2` for literally everything.
* Capabilities of the pipenet scale with GT tech level without having to replace a lot of blocks.

### design

## Cable

A `cable` is a custom pipe block that forms its own network. When placed, it auto-connects to other cables and to inventories. Cables that connect to an inventory are called "endpoints." Each endpoint has 6 "faces", one for each cardinal direction where it may connect to an inventory.

Multiple connecting `cables` create a network. This network allows players to configure transport of items/fluids between its endpoints.

When placed, endpoints have no capacity and cannot pull. They must be configured with GregTech items to allow input/output and GregTech covers to pull from the adjacent inventory.

## Covers and Transfer Speed

The pipenet created by `cables` does not have any capacity limit. The capacity of each point-to-point endpoint pair is equal to the smallest of their GregTech pipe capacity.

In order to pull from a connected inventory, like a chest or a drum, an _endpoint face_ must be fitted with a `conveyor` or `pump` GregTech item. This isn't placed like a cover, it is slotted into the endpoint's inventory like pipes are.

When using a cover to pull from a connected inventory, the transfer speed is the smallest of the point-to-point capacity and the cover's extract speed.

## Channels

Channels are very much as they are in EnderIO.

Each pipenet has one channel per minecraft color, as well as a null channel. Channels are disjoint logical networks within the pipenet, and defined per-face. Each blue face can only send/receive with other blue faces, etc.

## Endpoint Configuration

Each endpoint contains a single pipe configuration, and each _face_ has a cover, filter, and channel configuration. The pipe's capacity is applied to each connected face, rather than being split among them.

## TODO:

Cables should allow blocks that can _push_ items/fluids, like most GregTech machines and hatches, to do so through the network. _Pulling_ from the network shouldn't be possible. I need to look into the best way to accommplish this without voiding fluids or transmitting things faster than they should go.

One of my original ideas was introducing "volumetric capacity" as a logistical consideration and a potential way to introduce tiering into the cabling system (higher tier cables could provide more capacity).

All GregTech pipes have a transfer rate, but also a volume, with tiny pipes being 1x1, small 2x2, normal 4x4, etc up to 16x16. The idea was that endpoints would be volume limited (not _true_ volume; a 2x pipe would be 2x the "volume" of a 1x instead of 4x, etc), which would encourage use of higher tier pipe materials rather than just tossing in huge cheap pies everywhere.

This also fixes the problem of a single pipe's capacity being duplicated to all of the endpoint faces.

However, this punishes the tiny pipenets you use in early game, making them much more expensive than using GT pipes directly.

I still really like the volumetric capacity "puzzle" idea but if there's only 2 pipes per endpoint, then it isn't that interesting; it only places limits on using 16x pipes. If there is a "global pipe" and a "per-face pipe", I think the configuration UI starts to get confusing.