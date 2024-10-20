# Undo the Spire

<p align="center">
<a href="">
<img src="https://img.shields.io/badge/Install from Steam-000?logo=Steam&logoColor=white" alt="Install from steam" /></a>
<a href="https://discord.gg/TnxTeAw6S5">
<img src="https://img.shields.io/badge/Join the Discord server-5865F2?logo=Discord&logoColor=white" alt="Join the Discord server" /></a>
<a href="https://ko-fi.com/Q5Q314YFIG">
<img src="https://img.shields.io/badge/Buy me a shop relic-FF5E5B?logo=Ko-fi&logoColor=white" alt="Buy me a shop relic" /></a>
</p>


<p align="center"><img style="width:min(100%,max(80%,600px))" src="media/trailer.gif" alt="Loading clip..."/></p>

**Undo the Spire** is a *Slay the Spire* mod that adds a button to undo your actions in combat.

- Played a block card before you realised you had lethal? **UNDO!**
- Forgot you had Echo Form active and doubled a Strike instead of Biased Cognition? **UNDO!**
- Lost your Buffer to the Heart's Beat of Death? **UNDO!**

With so many interactions happening in the game, it's nearly impossible to be aware of everything at all times &ndash;
unless you're [a certain Vietnamese player](https://www.twitch.tv/xecnar), of course.
Mistakes are inevitable, and it's especially frustrating when a tiny misplay has the potential to ruin your entire run.
If you have ever experienced this &ndash; and ended up resorting to <s>savescumming</s> accepting your fate and
restarting, after your game decided to crash mid-fight &ndash; this mod is for you.


## What does it do?

**Undo the Spire** literally only does one thing: add buttons to undo/redo your actions in combat.


## What does it not do?

- Allow undoing outside of combat (yet!).
- Work with controller (yet!).
- Guarantee that you boss swap into Pandora's Box every time (yet!).


## Installation

**Steam Workshop**

The easiest way to install **Undo the Spire** is via Steam Workshop.

1. Open the [Undo the Spire Steam Workshop page].
2. Click "Subscribe".
3. If prompted "Additional Required Items", click "Subscribe to all"; this will install other mods that **Undo the Spire** requires to function, namely [ModTheSpire][modthespire], [BaseMod][basemod], [StsLib][stslib], and [Save State Mod][savestatemod].
4. Run *Slay the Spire* from Steam, and select "Play With Mods" when prompted.
If you don't see this prompt after you launch the game, open *Slay the Spire*'s settings, and under *General → Launch options → Selected Launch Option* pick "Play With Mods" (or "Ask when starting game" if you want to decide on a per-launch basis).
5. You should now see a window with a list of mods.
Select BaseMod, StSLib, STS Save State Mod, and Undo the Spire, together with any other mods you want to use.
6. Click "Play".

If everything is set up correctly, you should now be able to play *Slay the Spire* with **Undo the Spire**.
If you encounter any issues, please refer to the [Troubleshooting](#troubleshooting) section below.


**Manual installation**

If you want to try a pre-release version of **Undo the Spire**, you can install this mod manually.

1. Open the *Slay the Spire* Steam Workshop and subscribe
   to [ModTheSpire][modthespire], [BaseMod][basemod], [StsLib][stslib], and [Save State Mod][savestatemod].
2. Download your desired version of **Undo the Spire** from ??.
3. TBC...


## Compatibility

As a rule of thumb, there is a good chance that **Undo the Spire** will be compatible with quality-of-life (QoL) mods
that don't introduce new content.
However, it’s less likely to be compatible with mods that add new cards, characters, relics, or entirely new mechanics.

Here is a list of mods that are officially compatible with **Undo the Spire**:

- [BaseMod][basemod] and [StsLib][stslib];
- [RNG Fix][rngfix];
- [Relic Stats][relicstats].

Here is a list of mods that will almost certainly *never* be compatible with **Undo the Spire**:

- [Together in Spire](https://steamcommunity.com/sharedfiles/filedetails/?id=3148331689).

If your favourite QoL mod does not appear in either of the two lists, feel free to reach out! If there’s enough
interest, I may consider working on compatibility.

For content mods, compatibility is trickier due to the sheer number of them.
While I won’t be able to individually adapt Undo the Spire for content mods, creators can ensure compatibility by
implementing the necessary interfaces for [Save State Mod][savestatemod].
If you're a mod creator and need help with this, don’t hesitate to contact me (I might also create a wiki page on this
in the future).


## Troubleshooting

**If the game crashes at startup or whenever you perform or undo an action**

- Make sure that you've selected all the required dependencies for **Undo the Spire** in the ModTheSpire launcher.

**If you don't see undo/redo buttons in combat**

- Make sure you're playing the game with mods (there should be a faint text in the bottom left or top right corner
  saying "ModTheSpire" amongst other things).
- Make sure you've selected **Undo the Spire** in the ModTheSpire launcher.
  To verify this, click on "Mods" in the main menu and check that "Undo the Spire" appears in the list on the left.

**If you experience performance issues**

- **Undo the Spire** shouldn't affect performance too much, if you have other mods installed try and disable them first
  to see if the issue persists.
- In certain scenarios &ndash; for instance, a Donu and Deca fight lasting hundreds of turns &ndash; the Exhaust Pile
  can grow significantly.
  If you've exhausted over 1,000 cards in a single combat, creating a new undo state may become resource-intensive,
  depending on your hardware.
  If you anticipate this situation (e.g., your deck relies on an infinite combo), consider reducing the maximum number
  of undos in the mod settings.
  You can adjust these under: *Main menu → Mods → Undo the Spire → Config*.

If the steps listed above do not solve your issue, it's possible you've encountered a bug!
Please follow the steps in [the section below](#reporting-bugs-and-feature-requests) (or join
the [Discord server][discord-general]) to report it.


## Contributing

**Use the mod**<br/>
The best way to support this mod is simple: just use **Undo the Spire**!
Download the mod, launch the game, and get slayin'!
While **Undo the Spire** has been thoroughly tested and should work smoothly in most situations, it's still in early
development.
With the vast number of possible interactions between relics, cards, and enemies, it's impossible to test everything.
If you encounter anything that doesn't work as expected, please follow the guidelines
in [the section below](#reporting-bugs-and-feature-requests) to report a bug (or just jump in
the [Discord server][discord-general]!).

**Help translating**<br/>
Want to help bring **Undo the Spire** to players in other languages?
The amount of text in the mod is minimal, so even a small contribution can make a big impact.
If you're interested in translating the mod, feel free to reach out on the [Discord server][discord-general]!

**Compatibility with other mods**<br/>
If you’re the creator of another mod and would like to explore compatibility between **Undo the Spire** and your mod,
I’d love to collaborate: jump in the &ndash; you guessed it &ndash; [Discord server][discord-general] and drop me a
message!
While compatibility can’t always be guaranteed, I aim to make **Undo the Spire** as accessible as possible.

**Go the extra mile**<br/>
If you really love **Undo the Spire** and want to support its development, you could

<p align="center"><a href='https://ko-fi.com/Q5Q314YFIG' target='_blank'><img style='border:none;height:36px' src='media/support-me.png' alt='Buy me a shop relic' /></a></p>

It will make my day!


## Reporting bugs and feature requests

The easiest way to report a bug or request a feature is to join the [Discord server][discord-general].
There are two dedicated channels, one for `🐞bugs` and one for `💡feature-requests`.

If it's more convenient, feel free to [open an issue on GitHub](https://github.com/filippobaroni/undo-the-spire/issues)
instead.

When reporting a bug:

- if the game crashes, make sure to include the ModTheSpire console output (that is, the text in the white window that
  shows after the game crashed);
- include as much information as possible, such as an accurate description of what you did before the bug occurred, and
  if possible instructions on how to replicate the bug;
- include a screenshot or a video of the bug if relevant.

If you are unsure whether something is significant enough to be reported as a bug, here's a simple rule of thumb: **it
is**.
I'm a perfectionist, and I once spent a whole afternoon trying to make sure that undoing an action that wakes Lagavulin
up would reset the music correctly.
Whether it's a relic not flashing, an animation being too slow or too fast, if you've noticed it, report it; I will be
grateful.


## Philosophy

If your first reaction to this mod is to write a 5,000-word essay on why an undo button contradicts the spirit of *Slay
the Spire*, know that you're not alone.
But before you do, here’s my 5,000-word counter-essay on why **Undo the Spire** is actually a positive addition to the
game’s ecosystem.

**This mod isn't for everyone, and that's OK.**<br/>
It's undeniable that some players won't need or enjoy this mod, and that's perfectly fine.
If you're experienced enough to always be aware of the thousands of possible interactions that might trigger at any
point during the fight, this mod isn't for you.
If you’re the type of player who doesn’t mind when a small misplay ruins your run, or you value those mistakes as
learning opportunities, this mod isn't for you.
If your Kantian conscience won't let you avoid paying the price for your errors, this mod isn’t for you either.
All these perspectives are valid, and the last thing I want is to force this mod on anyone.

However, there are players such as myself who take every run seriously, but don't want to triple-check every buff,
debuff, and relic before playing each card.
For those of us who prefer a more relaxed approach, without the constant fear of throwing the run with a single silly
mistake, **Undo the Spire** provides peace of mind and an overall more enjoyable experience.

**But isn't it cheating???**<br/>
And if it is, so what?
*Slay the Spire* is a single-player game.
Whether or not someone uses an undo button doesn't affect your experience in the slightest.
The ultimate goal of this (or any) game is to have fun, so why not let people play the way they enjoy most?

In fact, I would argue that an undo button can actually *reduce* the amount of cheating when used appropriately.
*Save scumming* &ndash; saving and quitting to restart a fight &ndash; is already part of the base game and widely used
to correct critical misplays.
Ideally, after restarting a fight, you would to replicate every action you made until before the fatal mistake.
But this comes with two problems:

1. it's tedious, and the longer the fight, the easier it is to mess something up; *Slay the Spire*'s mechanics can cause
   even slight differences to lead to wildly different outcomes in the long run;
2. the temptation to use information that was only revealed later in the fight is always lurking ("This Colorless Potion
   is going to give me Dark Shackles, I should probably wait and use it next turn, when the Heart multi-attacks...").

**Undo the Spire** makes reverting a mistake as easy as it can be &ndash; literally the press of a button &ndash;
completely solving the first issue.
Of course, the temptation to "cheat" is still there, but the undo button makes the cheating more *intentional*: if you
decide to undo your Colorless Potion to wait one more turn, then you're undeniably using information you're not supposed
to have.
If this doesn't bother your conscience, more power to you &ndash; as I said, *Slay the Spire* is a single-player game,
live and let live.
But then, there are no excuses.

**Limited undoing on the roadmap.** <br/>
Some players might still feel uneasy about having an undo button so readily accessible.
After all, the flesh is weak, and if I *really* thought about this, I would not have used this Colorless Potion, knowing
that it *could* have given me Dark Shackles...
To address this, I plan to implement an option that only allows undoing when no new information has been revealed.
While this comes with its own set of challenges (for example, playing a card might trigger Ink Bottle, that would in
turn prevent undoing), it could offer a middle ground for players concerned with maintaining fairness.
In the end, it's up to you to decide how much (if at all) you want to use the undo button to improve your experience.


## Acknowledgments

In no particular order, I would like to thank all the people that made this project possible.

- The developers of *Slay the Spire*, for creating one of the most efficient time sinks of all time.
- The creators of ModTheSpire and BaseMod, for providing the foundation that made this and thousands of other mods
  possible.
- [boardengineer](https://github.com/boardengineer) for creating the Save State Mod, a crucial building block of **Undo
  the Spire**.
- My partner ❤️, for countless hours spent testing the earliest, bug-filled versions of **Undo the Spire**, even at
  the (very concrete) risk of their runs being ruined by crashes &ndash; or, even worse, by a subtle bug that caused
  enemies to occasionally drop slightly less gold.

[modthespire]: https://steamcommunity.com/sharedfiles/filedetails/?id=1605060445

[basemod]: https://steamcommunity.com/sharedfiles/filedetails/?id=1605833019

[stslib]: https://steamcommunity.com/sharedfiles/filedetails/?id=1609158507

[savestatemod]: https://steamcommunity.com/sharedfiles/filedetails/?id=2489671162

[rngfix]: https://steamcommunity.com/sharedfiles/filedetails/?id=2181005326

[relicstats]: https://steamcommunity.com/sharedfiles/filedetails/?id=2118491069

[discord-announcements]: https://discord.gg/nEfh6gGNvH

[discord-general]: https://discord.gg/TnxTeAw6S5