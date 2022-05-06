# PlaNET
The course project for CSCI3310 Mobile Computing and Application Development, which is a productivity app that allows users to prioritize their work.

## Software
Due to the limited Firebase quota, we do not release the software executable here. <br/>
Follow the steps below for building an APK file from the source code: <br/>
Step 1. Sign in to the <a href="https://firebase.google.com">Firebase Console</a> with your Google account and create a project <br/>
Step 2. In the Firebase Console, select "Project Overview" <br/>
Step 3. Add an Android application with the package name "edu.cuhk.csci3310.planet" <br/>
Step 4. Click "Register App" and follow the instructions to download the "google-services.json" file <br/>
Step 5. In the Sign-in providers tab of the Firebase console, enable email authentication <br/>
Step 6. Create a Firestore database in Cloud Firestore, change the security rules to the following: <br/>
```
service cloud.firestore {
  // Determine if the value of the field "key" is the same before and after the request
  function unchanged(key) {
    return (key in resource.data)
    && (key in request.resource.data)
    && (resource.data[key] == request.resource.data[key]);
  }
  match /databases/{database}/documents {
    // Works:
    // - Authenticated user can read
    // - Authenticated user can create/update/delete
    // - Validate create/update/delete
    match /works/{workId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
      && request.resource.data["email"] == request.auth.token.email;
      allow update: if request.auth != null
      && (request.resource.data.keys() == resource.data.keys())
      && unchanged("email");
      allow delete: if request.auth != null
      && resource.data["email"] == request.auth.token.email;
    }
  }
}
```
Step 7. Replace "app/google-services.json" with the JSON file downloaded in the last step <br/>
Step 8. In Android Studio click "Build > Build Bundles(s) / APK(s) > Build APK(s)" <br/>
Step 9. The software executable file is in "app/build/outputs/apk/debug/app-debug.apk" <br/>

## Functionalities
<ol>
  <li> To-do List: users can insert tasks and obtain a to-do list sorted in ascending order of deadline</li>
  <li> Searching and Filtering: users can search tasks with their name / tags and filter tasks according to their progress / importance / deadline</li>
  <li> Dashboard: users can view their task statistics and get recommended work from the app</li>
  <li> Notification: users can choose to receive a notification a certain period before the deadline of a task</li>
  <li> Customize Theme: users can select the theme of the planner and the icon of their tasks, which allows them to customize their planner
  <li> Authentication: users can register accounts and prevent others from accessing their app data</li>
</ol>

© 2022 Andes Kei and Tracy Chan
