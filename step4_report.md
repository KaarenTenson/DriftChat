### Testing strategy

**Unit tests:** <br>

**UI tests:** <br>

### Build process for APK

In Android Studio: <br>
Build -> Choose APK -> Next -> Key store path to key file -> Key store/Key password -> Next -> choose path -> <br> click 'release' -> Create

### Known bugs or limitation

* You sometimes get matched with yourself in the chatroom
* Database code needs to be optimised, because Firebase database does too many reads
* Video streaming doesn't work, you can sometimes receive the audio feed of your matched person
* Chosen quote isn't displayed on user info screen
* Chatroom matches people in random, doesn't take into account two users similarities.
