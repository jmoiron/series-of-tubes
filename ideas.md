# series of tubes

### _it's not a dump truck_

Brainstorm file for an advanced item/fluid/power for GregTech that builds off of GT's pipes, covers, and cables, but offers logistics more in line with what more powerful transport mods provide (eg. eio conduits, SFM, laserIO, Xnet)

## Bundle

A `bundle` is a new type of pipe block that forms its own network. When placed, it connects to other `bundles`, but not to other kinds of inventories.

A `bundle` creates its own network of connected bundles, but that network itself doesn't transport anything. Instead, it carries one or more _pipe subnets_, which are defined by pipes and cables present in each bundle's inventory. This means that a single bundle network could contain disjoint subnets of the same type.

The subnet capacity is not a fixed number. Each _tier_ of bundle has a _total volume_, with pipes and cable sizes occupying different volumes based on their physical size, making higher tier bundles more capable and higher tier pipe materials more volume efficient.

## Covers
