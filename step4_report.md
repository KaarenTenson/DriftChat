### Testing strategy

**Unit tests:** <br>

**UI tests:** <br>

UI testing strategy checks if the navigation between different screens functions as it is supposed to. By swiping to a screen and then checking for an element that is supposed to exist on that screen. By doing that we can check if swiping right or left actually changed the screen and if the screen we ended up on is the one we wanted to actually arrive on.

### Build process for APK

In Android Studio: <br>
Build -> Choose APK -> Next -> Key store path to key file -> Key store/Key password -> Next -> choose path -> <br> click 'release' -> Create

### Known bugs or limitation

* You sometimes get matched with yourself in the chatroom
* Database code needs to be optimised, because Firebase database does too many reads
* Video streaming doesn't work, you can sometimes receive the audio feed of your matched person
* Chosen quote isn't displayed on user info screen
* Chatroom matches people in random, doesn't take into account two users similarities.
