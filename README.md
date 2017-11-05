Droidule
=======

Unofficial Android app for Xedule using the unofficial [Xedule API](https://git.yildri.nl/Baconator/xedule-api), based on [Xedroid](https://github.com/Darkwater/xedroid) by Sam Lakerveld.


The Difference™
=======
### Changes
- Changed the star icon to home icon because I think it's more fitting.
- Fixed a lot of deprecated methods.
- Lots of refactoring to my preference (Still kinda WIP).

### New Features 🎉
- A fancy theming engine with currently 3 themes. Screenshots coming soon ;)
- Opening a schedule during the weekend will now show next week's schedule (Like Xedule does).
- The first opened schedule will now be marked as the default (starred) until changed.


### Sam's mess I had to clean up 😛
- Fixed a bug where it's impossible to scroll up on the attendee selection screen.

### New messes I've made
~~- When selecting a weekend date, the date will change to next week monday. I'd rather still have the weekend selected, but just showing next week.~~ :)
- Probably a ton more but I just haven't found it yet.

Building
--------

1. Clone repository.
2. Run `./gradlew installDebug` to install a debug build on any connected devices.

Note: `git describe --tags` is used for version number. Make sure `git` is accessible like that and your local
repository has the correct tags set. Try `git pull --tags` if something goes wrong.
