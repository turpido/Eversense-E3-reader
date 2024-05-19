# Sugar-Transmit Eversense E3
The Eversense E3 is a very nice diabetic solution to have BG monitoring. sinse the last Eversense XL sensor the company stop users from being able to read their glucose from the app for all sort of purpuses.
I found a way to read live the sugar data without tamping into the app itself by using the read system notification method available to android devices.
this app will send sugar data from phone to supabase by reading system notification, filtering only the Eversense app notification, reading notification data to get current BG level and transmit the data to supabase server
when open the app the app will immidiatly close, thats because we dont need the app to run in the foreground, we only need the background.
the app will show a notification to show that its alive and working. 
on this notification there will be a "stop" button that will stop the background service. to reactivate the service press the app again and the notification will rerurn.
in order to use this app you'll need to set up a supabase envierment and connect to it by adding SUPABASE_ANON_KEY and SUPABASE_URL to local.properties.
the app currently reads Eversense E3 notification and send them to supabase to store and use live.
for my use case the app reads my sugar level, and catch if i get a calibrate notification to update iswell.
I personaly use this app to display my glucose on a portable clock display, sitting under my pc monitor, so my phone can stay in my pocket while i constantly see my glucose trend right in front of me.
good luck to all who try this app out!
![sugar_display](https://github.com/turpido/Eversense-E3-reader/assets/48402145/779ac889-efc9-4c8a-99a0-3c70ea47fe1f)
