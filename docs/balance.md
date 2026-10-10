# Souls, levels and balance

Souls and levels. A level costs `100 + 20 n + 2 n^2` souls, `n` being the level less one (`PlayerSoul.levelCost`): it grows
with the square of the level. (It used to grow by a tenth of itself each level, which is the same to the thirties and
past the fifties outran anything the dead could leave: 140 thousand for one level in the seventies.) What a place pays
is tuned to that in `tools/balance.py`, which says how a part of the map is taken to be played (`CLEARED`, `PASSES`,
`LOST`: three quarters of it fought, more than once because of the vain tries, a tenth lost in stains; and `FARM`, a
quarter of a clearing more) and what level each lord is meant to be met at (`TARGET`). The rule for a new place: ten
levels, six before its first lord and four before its second; a lord leaves about two and a half times the cost of the
level he is met at; the dead of the place are priced so that the script's "meant" line wants about nothing more.

Balance (see `tools/balance.py`): one clearing of the town brings a player to about level 10-12 before the Gravedigger;
the lower fortress to about 18 before the Overseer; the upper city to about 29 before the Chaplain; the quarry to
about 35 before the Taskmaster and 39 before the Stone-Eater; the Mire to about 46 before the Wallower and 50 before
the Weeping Mother; the Firwood to about 55 before the Shatun and 60 before the Elder; the Wharf to about 66 before
the Helmsman and 70 before the Capsized; the Stubble to about 76 before the Scorched Warden and 80 before the Hollow
Miller; the Sky Beneath to about 86 before the Bolt and 90-91 before the Coming (its second half pays a little over
the rule: the Coming is the last lord, and nothing of the place is sold or forged yet). The fortress was
first tuned for a newcomer; its tiers make up for the levels a player now brings to it. When mobs, weapons, tiers or
the map change, run it again.
