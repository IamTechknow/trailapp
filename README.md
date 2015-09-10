# Wetlands Watcher

An Android application with a Google Maps interface to display trail information and data.

# Build notes
This project has dependencies with the Google Play Services and AppCompat library, and is configured in the old Ant Build model instead of Gradle. To build properly in IDEA, find the two library directories and add them as modules, then on Dependencies for the trailapp module, add the modules, JARs, and libraries.

Next, make sure the appcompat module SDK is API level 21 or up. Now you can build the project!

# Acknowledgements
This project draws ideas from Niantic Lab's Field Trip