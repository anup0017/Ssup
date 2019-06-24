# **Ssup : Android Chat Application using Android Studio and Firebase.** 
### Getting Started with Your Own App:
- Open Android Studio. 

![Android Studio](https://vignette.wikia.nocookie.net/android/images/f/fb/Android_Studio_icon.svg.png/revision/latest?cb=20180401073355)
- Create a new project.![New Project](https://developer.android.com/training/basics/firstapp/images/studio-welcome_2x.png)
- Create a new project in [Firebase](https://firebase.google.com/).
 ![Firebase](https://www.xda-developers.com/files/2016/05/Googles-Firebase-is-Expanding-to-a-Unified-App-Platform.png).
- Connect your Android Studio Project with the Firebase. Your build.gradle(app) should have these included:
  	```
  	implementation 'com.google.firebase:firebase-auth:16.0.5'
    implementation 'com.google.firebase:firebase-database:16.0.4'
    implementation 'com.google.firebase:firebase-storage:16.0.4'
    implementation 'com.google.firebase:firebase-messaging:17.3.4'
    implementation 'com.firebaseui:firebase-ui-database:4.2.1'
	```
	
- Create a real time database in your firebase project. Structure of my database: 
 ![Real-time database](https://firebasestorage.googleapis.com/v0/b/fir-notification-3d8ea.appspot.com/o/database.png?alt=media&token=5dd6bfb8-2d0c-4fa1-970d-895853c44c06)
 
- After being done with the front end development, it's time to implement push notifications onto the app. Notifications in this project is being implemented using `Firebase cloud functions`(using javascript).
- To push the javascript to the cloud functions, install [node.js](https://nodejs.org/en/download/).
- Open node.js command prompt and install firebase tools.
    `npm install -g firebase-tools`
- Login to firebase in the command prompt.
    `firebase login`
- Create a folder and inside that folder, initialize the firebase.
    `firebase init`
- Add the javascript code for getting push notification for messages on the device in **_index.js_** file and deploy it to the firebase cloud function.
 `firebase deploy`

## Key Features:
- Login via email or phone number, set user name, status and a profile picture.
- Send chat requests to your friends.
- You can now send messages, images or files to your friends.
- Delete for me/Delete for everyone feature included.
- Group chat feature available.
- Get notification on your device when someone sends you a message or a chat request.

## Some useful links: 
- https://github.com/hdodenhof/CircleImageView 
- https://firebase.google.com/docs/auth/android/phone-auth
- https://github.com/ArthurHub/Android-Image-Cropper
- http://square.github.io/picasso/
