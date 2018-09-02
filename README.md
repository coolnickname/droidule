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
- The first opened schedule will now be marked as the default (starred) until changed


Feedback
-------
If you have found a bug, please make sure it's not already filed under [issues](https://git.yildri.nl/Baconator/droidule/issues?scope=al&state=opened&search=%5BBug%5D). If it's a new one, first of all I'm sorry to have let your down. Second I'd like you to [mail me](mailto:yildri@yildri.nl) about it.

For any other feedback or if you want to help me with this project, feel free to mail me at [yildri@yildri.nl](mailto:yildri@yildri.nl).

Building
--------

1. Clone repository.
2. Run `./gradlew installDebug` to install a debug build on any connected devices.

Note: `git describe --tags` is used for version number. Make sure `git` is accessible like that and your local
repository has the correct tags set. Try `git pull --tags` if something goes wrong.
