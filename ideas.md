



# series of tubes

### _it's not a dump truck_

Brainstorm for an advanced item/fluid/power for GregTech that builds off of GT's pipes, covers, and cables, but offers logistics more in line with what more powerful transport mods provide (eg. eio conduits, SFM, laserIO, Xnet)

## Bundle

A `bundle` is a new type of pipe block that forms its own network. When placed, it auto-connects to other `bundles`, but not to other kinds of inventories.

t's not a dump truck_

This is a brainstorming document for an advanced item/fluid/power distribution mod for GregTech that builds off of gregtech's existing pipes,
fitlers, and cables, but offers simpler logistics in line with what more powerful transport mods provide (eg. eio conduits, SFM, laserIO, Xnet)

### goals

* GregTech packs would rather use SoT (alt name: _chubes_) than something like `eio` or `laserIO` as it integrates better with GT.
* Players get flexible, powerful logistics without having to use `applied-energistics-2` for literally everything.

### design ideas


## Bundle

A `bundle` is a new type of pipe block that forms its own network. When placed, it auto-connects to other `bundles`, but not to other kinds of
inventories.

Multiple connecting `bundles` create a bundle network, but that network doesn't transport anything. Instead, it carries one or more _pipe subnets_,
which are defined by pipes and cables present in each bundle's inventory. A single bundle network could contain disjoint subnets of the same type.

Each _pipe type_ in a bundle defines the available subnets for that bundle. Bundles can have multiple subnets defined by the same _pipe type_, eg.
3 `tin item pipes` defines 3 distinct item transport subnets.

The subnet capacity is not a fixed number. Each _tier_ of bundle has a _maximum volume_, with pipes and cable sizes occupying different volumes
based on their physical size, making higher tier bundles more capable and higher tier pipe materials more volume efficient.

## Connections

Each `bundle` has 6 faces. Like bundles, _pipe subnets_ will automatically connect to each other when placed in adjacent bundles. Unlike GT pipes,
this is not dependent on which face you've placed the pipe against.

All connections (including these automatic ones) can be enabled and disabled on a per-face, per-subnet basis. These connections can be directional,
like GT pipes are. Adjacent bundles _cannot_ be disconnected.

It may be difficult to debug large bundle networks until a good renderer surfaces disconnected adjacent subnets visually.

## Covers and Transfer Speed

A _pipe subnet_ cannot pull from connected inventories automatically. Instead, each _connection_ must be configured with an appropriate cover (eg
a _pump_ or a _conveyor_). Transfer speeds are determined by the maximum of the cover and the subnet transfer speed.

Items and fluids do not transit their _subnets_ one block at a time; they are "teleported" to the highest priority endpoint (covers have some
configuration options here). This is a big departure from GT, especially for fluids.

