# AccPbFRET Changelog
*This changelog only lists notable changes for the end user. For all changes, please see the commit history.*

## 4.0.0-SNAPSHOT (Unreleased)

### New features
* Save transfer (FRET) images from the main window.
* Copy region of interests (ROIs) in the correction factor calculation windows.

### Important changes
* Negative pixel values are now included when calculating correction factors.
* Default Gaussian blur values are now 0.4x previous values.
* Background subtraction and Gaussian blurring steps have been reordered.
* Spectrum LUT is no longer applied automatically.
* AccPbFRET now uses Maven. The result is a single distributable JAR.
* _Check for updates_ has been removed.
* _Help_ now points to https://imagej.net/AccPbFRET.

#### Deprecation
Old | New
--- | ---
`GaussianBlur.blur()` | `GaussianBlur.blurGaussian()`
`ResultsTable.addLabel()` | `ResultsTable.setValue()`
`Java.util.Date` | `Java.time`

### User Interface (UI) changes
* Added: Scroll bars to accommodate new features.
* Changed: Improved hard-coded window sizes, particularly on macOS.
* Changed: Use native looking file chooser for I/O.
* Changed: Revised menu.
* Changed: Semi-automatic processing window UI.
* Changed: Various tweaks.
* Fixed: Various typos.

### Logging improvements
* Log image names when setting channels.
* Log sigma values when applying Gaussian blur.
* Log saving the log!

### Bug fixes
* Fixed: Blurring the _acceptor after bleaching_ would use the value from the _acceptor before bleaching_ text field.
* Fixed: Button color now changes to green once clicked on macOS.
