#####0.21#####
changes:

 - casting should no longer crash the app

#####0.2#####
changes:

 - now correctly accounts for daylight savings time
 - added MMA Pro, MMA SR+, StreamTVnow services
 - renamed servers and services to fall in line with Kodi plugin
 - removed RTSP option
 - update some of the dependencies

#####0.164#####
changes:

 - video player can be either in 'landscape' or 'reversed landscape'

#####0.163#####
changes:

 - can now start casting form inside the local media player

#####0.162#####
changes:

 - more chromecast crash fixes

#####0.161#####
changes:

 - more chromecast improvements

#####0.160#####
changes:

 - scrolling should be smoother, especially in the case where there are a large number of events/search results
 - chromecast experience should be better (less crashes when disconnecting from chromecast, etc)

#####0.158#####
changes:

 - better exception handling when retrieving latest stream data. this should prevent some crashes

#####0.157#####
changes:

 - oops. url DECODING is whats needed on stream url

#####0.156#####
changes:

 - url encoding on user/pass when generating stream url

#####0.155#####
changes:

 - event list should now update on refresh

#####0.154#####
changes:

 - ability to clear search history in settings
 - upgrade support libraries
 - minor layout tweaks

#####0.153#####
changes:

  - search results are no longer cleared after 'set alert' dialog pops up
  - search results are now sorted by air date
  - minor tweak to channels tab so that channel names line up

#####0.152#####
changes:

  - more work to remove the crash that happens when app is not running or in background

#####0.151#####
changes:

 - fix crash that happens every so often when app is in the background
 - port fix (hopefully) for mystreams HLS

#####0.15#####
changes:

 - added debug mode to help with troubleshooting. launching of streams does not work with debug enabled
 - when searching, past queries show up.

#####0.14#####
changes:

 - tapping on alert notification will open up the app
 - added search and tapping on results that happen in the future open alert dialog

#####0.13#####
first public release!

changes:

 - users can select the protocol (HLS/RTSP/RTMP) to open streams up locally. casting a stream will still be HLS regardless of setting
 - more layout tweaks

#####0.12####
changes:

 - only prompt to set an alert if the event is in the future
 - changed source of stream info

#####0.11####
changes:

 - can now set alerts as a reminder for upcoming events through the Events tab
 - misc crash fixes

#####0.1#####
changes:

 - apk size reduction through optimization
 - update support libraries
 - minimum Android version is now ice cream sandwich (4.0)

#####0.092#####
changes:

 - theme changes
 - should fix possible crash that happens when stream data is fetched in the background
 - background stream data increased from 15 minutes to an hour

#####0.091#####
changes:

- bulk insert of events and channels
- channel and event data properly fetched every 15 minutes

#####0.09#####
changes:

 - crash fix on Lollipop devices
 - rework channel and event fetching
 - refresh button added
 - ensure no duplicate days/events in events list

#####0.082#####
changes:

 - crash fix

#####0.081#####
changes:

 - added crashlytics

#####0.08#####
changes:

 - user-agent sent on requests
 - okhttp instead of httpurlconnection
 - picasso instead of universal image loader

#####0.07#####
changes:

 - check for internet connection before tasks that require one
 - other misc fixes

#####0.06#####
changes:

 - handle umlauts in event names
 - event list will update after task to get channel info is completed
 - flags to indicate event broadcast language if applicable

#####0.051#####
changes:

 - fixes crashes regarding mini controller when switching fragments
 - small layout changes

#####0.05#####
changes:

 - hd badged changed to text instead of image
 - added tabs with list of events

#####0.04#####
changes:

 - channels display an event title when there is an event currently airing
 - events broadcasted in 720p are marked

#####0.03#####
changes:

 - channels are added to channel list as soon as they are parsed (backend change: now using content provider)
 - better handling of the app on devices that do not have google services (e.g. Fire TV)

#####0.02#####
changes:

 - softkeys hide on tablets
 - checks to make sure credentials are set
 - slightly better launcher icon
 - do not start video player if returning from another activity (i.e., chromecast controller or external player)
