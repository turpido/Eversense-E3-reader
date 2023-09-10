# Sugar-Transmit Eversense E3
The Eversense E3 is a very nice diabetic solution to have BG monitoring. sinse the last Eversense XL sensor the company stop users from being able to read their glucose from the app for all sort of purpuses.
I found a way to read live the sugar data without tamping into the app itself by using the read system notification method available to android devices.
this app will send sugar data from phone to firebase by reading system notification, filtering only the Eversense app notification, reading notification data to get current BG level and transmit the data to firebase server
when open the app the app will immidiatly close, thats because we dont need the app to run in the foreground, we only need the background.
the app will show a notification to show that its alive and working. 
on this notification there will be a "force stop" button that will stop the background service. to reactivate the service press the app again and the notification will rerurn.
in order to use this app you'll need to set up a google firebase account and add the google-services.json file to the project app folder.
the app currently only reads Eversense E3 notification and send them to firebase to store and use live.
I personaly use this app to display my glucose on a portable clock display, sitting under my pc monitor, so my phone can stay in my pocket while i constantly see my glucose trend right in front of me.
good luck to all who try this app out!
