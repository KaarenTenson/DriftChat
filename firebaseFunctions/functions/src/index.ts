import * as admin from "firebase-admin";
import * as functions from "firebase-functions/v2/firestore";
import {getFirestore} from "firebase-admin/firestore";

const app = admin.initializeApp();
const db = getFirestore(app);
exports.matchWaitList = functions.onDocumentCreated(
  "waitList/{userId}",
  async (event) => {
    const newUserId = event.data?.id;
    if (!newUserId) {
      return;
    }

    try {
      await db.runTransaction(async (transaction) => {
        const querySnapshot = await transaction.get(
          db
            .collection("waitList")
            .where(admin.firestore.FieldPath.documentId(), "!=", newUserId)
            .orderBy("createdAt", "asc")
            .limit(1)
        );

        if (!querySnapshot.empty) {
          const matchedDoc = querySnapshot.docs[0];
          const matchedUserId = matchedDoc.id;

          const chatRoomRef = db.collection("chatRooms").doc();
          transaction.set(chatRoomRef, {
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            members: [newUserId, matchedUserId],
            lastMessage: null,
          });
          transaction.delete(db.collection("waitList").doc(newUserId));
          transaction.delete(db.collection("waitList").doc(matchedUserId));
          console.log(
            `chatRoom ${chatRoomRef.id}, users: ${newUserId}, ${matchedUserId}`
          );
        } else {
          console.log(
            `No match found for ${newUserId}. User remains in waitList.`
          );
        }
      });
    } catch (error) {
      console.error(`Transaction failed for user ${newUserId}:`, error);
    }
  }
);
