## API's
The app uses **Quotes API** from https://api-ninjas.com/api/quotes as it's public API.
This public API was used because it fits the theme for creating your profile.
The app makes only one GET request to the endpoint **v2/randomquotes** so the user could choose a random quote for their profile.

This is serviced by Retrofit, because it can automatically convert the JSON to a data class via GSON converter.

The app also uses **GoogleAPI** to connect itself to Firebase's Firestore Database.
The whole chatroom's logic works through firestore, it has a collection for waitlist (when the user is looking for a chatting partner),
 chatroom (when the user is mathced with another user) and messages where each message has an chatroomID and a userID (document created when user entered waitlist).
 These messages are served by a listener.

## Error handling strategy
Network-related issues are managed through a real-time NetworkMonitor that continuously tracks connectivity using Android’s ConnectivityManager and a socket-based reachability test, ensuring accurate detection of internet availability. When connectivity is lost, the app notifies the user via a Toast or UI banner and automatically recovers once the network is restored. At the screen level, components such as ChatRoomScreen and UserDataScreen handle validation and display errors directly in the UI through elements like ErrorBox overlays and inline input feedback, preventing crashes and preserving usability.
