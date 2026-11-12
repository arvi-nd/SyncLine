# SyncLine — Firebase Architecture Audit Report

**Date:** October 27, 2026  
**Scope:** Firebase Authentication, Firestore Database, Data Layer (`UserRepository.kt`), State Management (`GlobalChatViewModel.kt`), Security Rules (`firestore.rules`), and Navigation Architecture.  
**Mode:** Strictly Read-Only Audit & Inspection Report.

---

## Executive Summary

SyncLine currently implements a functional Firebase backend for **Authentication**, **User Profile Persistence**, **Unique Username Reservation**, **Connection Invitations (Chat Requests)**, and **Bilateral Client Relationship Sync**. 

However, **Real-Time Messaging is completely unpersisted and mock/memory-driven**. When a user types and sends a chat message, it is written only to an in-memory `MutableStateFlow<List<Message>>` inside `GlobalChatViewModel.kt`. No Firestore collection exists for conversations or messages, meaning messages do not synchronize across devices and disappear when the app process is terminated.

### Key Architectural Findings & Risk Summary
1. **Unpersisted Messaging Layer (Confirmed)**: `sendMessage()` writes exclusively to in-memory state. There are zero Firestore reads, writes, or snapshot listeners for chat messages or conversation threads.
2. **Missing Conversation Document Entity (Confirmed)**: While `ChatRequest` establishes a relationship (`ACCEPTED`), no shared `conversations/{conversationId}` document is generated to host real-time chat metadata (last message, unread counters, participant UIDs).
3. **Overly Permissive User Profile Read Rule (Confirmed)**: `firestore.rules` allows any authenticated user to read all documents under `/users/{userId}`, exposing email addresses and profile metadata across accounts.
4. **Non-Atomic Relationship Writes (Confirmed)**: `acceptChatRequest()` writes to `/users/{senderUid}/clients/{targetUid}` and `/users/{targetUid}/clients/{senderUid}` using two separate, un-batched `.set()` operations without a Firestore transaction.
5. **Dual Listener Overhead for Requests (Confirmed)**: `observeChatRequests()` launches two separate continuous snapshot listeners (`whereEqualTo("targetUid", myUid)` and `whereEqualTo("senderUid", myUid)`) and merges them in memory via Kotlin `combine`.

---

## Current Firebase Tree

```
Firestore Root
│
├── 📁 users/{uid}                          [Document: UserProfile]
│   └── 📁 clients/{clientId}                [Document: ClientRelationship]
│
├── 📁 usernames/{username}                 [Document: UsernameIndex]
│
└── 📁 chat_requests/{requestId}             [Document: ChatRequest]
```

*Note: No `conversations` or `messages` collections exist in Firestore.*

---

## Data Schema Inventory

### 1. User Profile
- **Path:** `users/{uid}`
- **Purpose:** Primary user profile document tied 1:1 with Firebase Auth UID.
- **Model Class:** `com.example.model.UserProfile`
- **Doc ID Strategy:** `auth.currentUser.uid`
- **Fields Written:** `uid` (string), `email` (string), `displayName` (string), `username` (string), `createdAt` (server timestamp), `updatedAt` (server timestamp).
- **Writers:** `UserRepository.ensureProfile()` (line 103)
- **Readers:** `UserRepository.getProfile()`, `UserRepository.observeProfile()`
- **Status:** **Active & Functional**

### 2. Client Relationship
- **Path:** `users/{uid}/clients/{clientId}`
- **Purpose:** Represents an established, accepted contact/partner in a user's client list.
- **Model Class:** `com.example.model.ClientRelationship` / mapped to `com.example.model.Client` in ViewModel.
- **Doc ID Strategy:** Partner user's Firebase UID (`clientProfile.uid`).
- **Fields Written:** `clientId` (string), `uid` (string), `username` (string), `displayName` (string), `email` (string), `createdAt` (server timestamp).
- **Writers:** `UserRepository.addClientRelationship()` (line 171)
- **Readers:** `UserRepository.observeUserClients()` (line 181)
- **Status:** **Active & Functional**

### 3. Username Index
- **Path:** `usernames/{username}`
- **Purpose:** Ensures username uniqueness and provides O(1) direct document lookups during "Add Client" search.
- **Model Class:** `UsernameIndex` (mapped via Map payload)
- **Doc ID Strategy:** Trimmed, lowercase username (e.g. `sarah_j`).
- **Fields Written:** `uid` (string), `username` (string), `displayName` (string), `email` (string), `updatedAt` (server timestamp).
- **Writers:** `UserRepository.ensureProfile()` (line 114)
- **Readers:** `UserRepository.getUserByUsername()`, `UserRepository.isUsernameAvailable()`
- **Status:** **Active & Functional**

### 4. Chat Request
- **Path:** `chat_requests/{requestId}`
- **Purpose:** Single-document request model tracking connection invitation state.
- **Model Class:** `com.example.model.ChatRequest`
- **Doc ID Strategy:** Synthetic key `${senderUid}_${targetUid}` (deterministic for a given directional request).
- **Fields Written:** `senderUid`, `senderUsername`, `senderDisplayName`, `senderEmail`, `targetUid`, `targetUsername`, `targetDisplayName`, `targetEmail`, `status` (`PENDING`, `ACCEPTED`, `DECLINED`), `createdAt`, `updatedAt`.
- **Writers:** `UserRepository.sendChatRequest()`, `UserRepository.acceptChatRequest()`, `UserRepository.declineChatRequest()`
- **Readers:** `UserRepository.observeChatRequests()`
- **Status:** **Active & Functional**

---

## Chat Lifecycle Trace

| Step | Action | Code Location | Firestore Path / Action | Execution Result |
|---|---|---|---|---|
| **1** | **Sign Up / Registration** | `SignUpScreen.kt` & `GlobalChatViewModel.signUpWithEmail()` (line 323) | `FirebaseAuth.createUserWithEmailAndPassword()` -> `UserRepository.ensureProfile()` -> `users/{uid}.set()` & `usernames/{username}.set()` | Auth account created, profile persisted, username indexed. |
| **2** | **Username Search** | `BottomNavBar.kt` -> `GlobalChatViewModel.addClientByUsername()` (line 181) | `UserRepository.getUserByUsername()` -> `usernames/{cleanUsername}.get()` | Resolves target user profile without querying full `users` collection. |
| **3** | **Send Request** | `UserRepository.sendChatRequest()` (line 211) | `chat_requests/${senderUid}_${targetUid}.set()` with `status = "PENDING"` | Prevents duplicates if `PENDING` or `ACCEPTED` exists. |
| **4** | **Observe Request** | `GlobalChatViewModel.syncChatRequests()` (line 107) | `UserRepository.observeChatRequests()` -> 2 x `chat_requests.snapshots()` | Both sender and recipient receive real-time request updates. |
| **5** | **Accept Request** | `NotificationsScreen.kt` -> `GlobalChatViewModel.acceptChatRequest()` | `UserRepository.acceptChatRequest()` -> `chat_requests/{id}.update("status": "ACCEPTED")` | Updates request state to `ACCEPTED`. |
| **6** | **Establish Bilateral Link** | `UserRepository.acceptChatRequest()` (lines 278-279) | `users/{senderUid}/clients/{targetUid}.set()` AND `users/{targetUid}/clients/{senderUid}.set()` | Creates contact documents for both users. |
| **7** | **Inbox Display** | `GlobalChatViewModel.syncUserClients()` (line 126) | `UserRepository.observeUserClients()` triggers ViewModel mapping to `InboxItem` | New client appears in Inbox with status `"Chat request accepted"`. |
| **8** | **Open Conversation** | `ChatScreen.kt` & `GlobalChatNavGraph.kt` | Sets `activeChatClientId = client.id` in ViewModel state | Loads in-memory message list filtered by `conversationId == client.id`. |
| **9** | **Send Message** | `GlobalChatViewModel.sendMessage()` (line 443) | `_messages.update { it + newMsg }` (In-memory state ONLY) | **UNPERSISTED**: Message exists in RAM only; lost on app restart. |
| **10** | **Activity Status** | `NotificationsScreen.kt` | Reads `chatRequests` StateFlow from ViewModel | Displays status chip (`Pending`, `Accepted`, `Declined`) in Activity tab. |

---

## Audit of Request & Conversation Identity

1. **Request ID Determinism**: `requestId` is computed as `${senderUid}_${targetUid}`.
   - *Confirmed*: Prevents multiple identical pending requests from `User A` to `User B`.
   - *Potential Risk*: Reverse requests (`User B` to `User A`) generate `targetUid_senderUid`, creating two independent request documents if both users invite each other simultaneously.
2. **Missing Conversation Key**: Currently, `client.id` (which is `targetUid`) is passed as `conversationId`.
   - *Confirmed*: `User A` sees `conversationId = User B's UID`, while `User B` sees `conversationId = User A's UID`. Because the IDs do not match, any shared Firestore query using `conversationId` without a standardized key (`${min(A,B)}_${max(A,B)}`) would fail to match between participants.
3. **Idempotency of Client Creation**: Calling `acceptChatRequest()` multiple times rewrites the same document path `users/{myUid}/clients/{partnerUid}` using `.set()`.
   - *Confirmed*: Idempotent and safe against duplicate document accumulation.

---

## Real-Time Listeners and Quota Review

### Active Firestore Listeners in `GlobalChatViewModel`
1. **User Profile Listener**: `userRepository.observeProfile(currentUid)`
   - Attached on Auth sign-in / user change; cancelled via `profileJob?.cancel()`.
   - Scoped strictly to `users/{currentUid}`. (1 document read on change).
2. **User Clients Listener**: `userRepository.observeUserClients(currentUid)`
   - Attached on Auth sign-in; cancelled via `clientsJob?.cancel()`.
   - Scoped strictly to `users/{currentUid}/clients`. (Reads only user's client list).
3. **Chat Requests Dual Listeners**: `userRepository.observeChatRequests(currentUid)`
   - Launches **2 concurrent listeners**:
     a. `chat_requests.whereEqualTo("targetUid", currentUid)`
     b. `chat_requests.whereEqualTo("senderUid", currentUid)`
   - Cancelled via `requestsJob?.cancel()`.
   - *Confirmed Inefficiency*: Fires two collection queries for every request update. Can be combined with a single query or composite indexes if optimized in future phases.

---

## Security Rules Audit (`firestore.rules`)

```sydney
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;

      match /clients/{clientId} {
        allow read, write: if request.auth != null && (request.auth.uid == userId || request.auth.uid == clientId);
      }
    }

    match /usernames/{username} {
      allow read: if request.auth != null;
      allow create, update: if request.auth != null && request.auth.uid == request.resource.data.uid;
    }

    match /chat_requests/{requestId} {
      allow read: if request.auth != null && (resource == null || resource.data.senderUid == request.auth.uid || resource.data.targetUid == request.auth.uid);
      allow create: if request.auth != null && request.resource.data.senderUid == request.auth.uid;
      allow update: if request.auth != null && (resource.data.targetUid == request.auth.uid || resource.data.senderUid == request.auth.uid);
    }
  }
}
```

### Security Findings
1. **Profile Data Exposure (Confirmed)**: `allow read: if request.auth != null;` on `/users/{userId}` permits any logged-in account to read any other user's complete profile document (including private `email`).
2. **Missing Field Schema Validation (Confirmed)**: Security rules check UIDs and status permissions, but do not validate string lengths, required fields, or timestamp constraints.
3. **Recipient Accept Restriction (Confirmed)**: `chat_requests` update rule permits both `senderUid` and `targetUid` to update request documents. To enforce zero-trust security, status changes from `PENDING` -> `ACCEPTED`/`DECLINED` should be restricted exclusively to `targetUid`.

---

## Messaging Readiness

| Component | Status | Required Action |
|---|---|---|
| **Firebase Auth** | **Ready** | Fully integrated with state listeners. |
| **User Profiles (`users/{uid}`)** | **Ready** | Persisted & synchronized via Firestore. |
| **Username Lookup (`usernames/{username}`)** | **Ready** | Index collection active and functional. |
| **Chat Request Flow (`chat_requests`)** | **Ready** | Complete with Activity tab status sync. |
| **Client Contacts Sync (`users/{uid}/clients`)** | **Ready** | Bilateral client list synchronized. |
| **Conversations Collection (`conversations`)** | **Missing** | Need Firestore schema for chat metadata. |
| **Messages Collection (`conversations/{id}/messages`)** | **Missing** | Need real-time Firestore messaging repository layer. |

---

## Recommended Migration Plan (For Future Phase)

1. **Standardize Conversation Key**:
   - Define a deterministic conversation ID for any two users: `val conversationId = if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"`.
2. **Add Firestore Message Data Layer**:
   - Create `conversations/{conversationId}/messages/{messageId}` collection schema.
   - Implement `sendMessage(conversationId, text)` writing to Firestore.
   - Implement `observeMessages(conversationId)` snapshot listener limited to recent N messages (e.g. `.orderBy("timestamp").limit(50)`).
3. **Use Firestore Batch Writes for Acceptance**:
   - Refactor `acceptChatRequest()` to use `db.batch()` to write request status update and both `users/{uid}/clients` documents atomically.
4. **Refine Firestore Security Rules**:
   - Restrict `/users/{userId}` reads or rely on `/usernames/{username}` for directory lookup.
   - Add rules for `/conversations/{conversationId}/messages/{messageId}` ensuring `request.auth.uid` is a participant.
