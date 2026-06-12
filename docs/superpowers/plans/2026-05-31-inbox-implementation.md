# Inbox Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a real Inbox (Message List) screen that shows all active conversations for the seller, replacing the placeholder test chat.

**Architecture:**
- **InboxActivity:** A new Activity that fetches from the `chats` Firestore collection where the seller is a participant.
- **RecyclerView & Adapter:** Displays a list of `ChatSession` objects. Clicking an item opens `ChatActivity` with the correct `chatId` and `buyerName`.
- **Navigation:** The bell icon in `MainActivity` will launch `InboxActivity`.

**Tech Stack:** Java, Firestore, Material Design.

---

### Task 1: Create Inbox Layout and Item Layout

**Files:**
- Create: `app/src/main/res/layout/activity_inbox.xml`
- Create: `app/src/main/res/layout/item_chat_session.xml`

- [ ] **Step 1: Create activity_inbox.xml**
Create a layout with a `MaterialToolbar` and a `RecyclerView` on a `bg_ice` background. Also add a "No messages" `TextView` that shows when the list is empty.

- [ ] **Step 2: Create item_chat_session.xml**
Create a `MaterialCardView` layout for each conversation item. It should show an avatar placeholder, the buyer's name (`txtBuyerName`), and a "View Chat" indicator (like an arrow icon).

- [ ] **Step 3: Commit Task 1**
```bash
git add app/src/main/res/layout/activity_inbox.xml app/src/main/res/layout/item_chat_session.xml
git commit -m "feat: add layouts for inbox screen"
```

---

### Task 2: Create InboxActivity and Adapter

**Files:**
- Create: `app/src/main/java/com/example/ruralize/InboxActivity.java`
- Modify: `app/src/main/java/com/example/ruralize/models/ChatSession.java`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Update ChatSession model**
Ensure `ChatSession.java` has an empty constructor for Firestore parsing, and fields for `buyerName` and `empresaId`. Add `chatId` which we will set manually from the document ID.

- [ ] **Step 2: Create InboxActivity.java**
Initialize `FirebaseFirestore` and query the `chats` collection. Assuming the web shop saves an `empresaId` or `sellerId` field, query: `db.collection("chats").whereEqualTo("empresaId", currentUserId)`. If it uses `sellerId`, adjust accordingly. Listen for updates and populate the `RecyclerView`. Add an `OnItemClickListener` to start `ChatActivity` passing the `chatId` and `buyerName`.

- [ ] **Step 3: Register InboxActivity in Manifest**
Add the activity to `AndroidManifest.xml` with the Ruralize theme.

- [ ] **Step 4: Commit Task 2**
```bash
git add app/src/main/java/com/example/ruralize/InboxActivity.java app/src/main/java/com/example/ruralize/models/ChatSession.java app/src/main/AndroidManifest.xml
git commit -m "feat: implement InboxActivity to list real chats"
```

---

### Task 3: Update MainActivity Navigation

**Files:**
- Modify: `app/src/main/java/com/example/ruralize/MainActivity.java`

- [ ] **Step 1: Change Toolbar Bell Click Listener**
Change the `action_notifications` click listener in `MainActivity.java` to start `InboxActivity` instead of `ChatActivity` with test data.

- [ ] **Step 2: Commit Task 3**
```bash
git add app/src/main/java/com/example/ruralize/MainActivity.java
git commit -m "feat: link notification bell to InboxActivity"
```
