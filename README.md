# Wetlands Watcher

An Android application with a Google Maps interface to display trail information and data. Demonstrates the use of Material Design, XML parsing, database storage, and background threading to handle KML data from the Watsonville Wetlands Watch.

# Build notes
This project has dependencies with the Google Play Services and Android Support library, as well as the Android Maps Utilities library. This is a Gradle based project which handles all the dependencies.

At the moment this project targets API level 22 and there will encounter build errors about a values-v23 folder for the appcompat library repository. The folder needs to be moved or deleted for the build to work

# Acknowledgements
This project draws ideas from Niantic Lab's Field Trip, and was first conceived at the Hack UCSC 2015 hackathon. More information may be found at the About screen.