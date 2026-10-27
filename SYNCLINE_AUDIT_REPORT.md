# SYNCLINE — COMPLETE APPLICATION AUDIT & BACKEND READINESS REPORT

**Project Name:** SyncLine (Global Chat / Wise Fintech-Style Messaging App)  
**Package / Namespace:** `com.example` / `com.aistudio.globalchat.vxkpzw`  
**Target Platform:** Android (Kotlin 2.0.21, Jetpack Compose, Material 3)  
**Audit Date:** October 26, 2026  
**Auditor Role:** Senior Full-Stack Engineer, Software Architect, UI/UX Auditor, Firebase Specialist & Security Engineer  

---

## 1. EXECUTIVE SUMMARY

SyncLine is an Android mobile client application built with Kotlin and Jetpack Compose, adhering strictly to the **Wise Design System** aesthetic (high-contrast Wise Lime `#9FE870`, Dark Green `#163300`, Near Black `#0E0F0C`, ultra-bold typography, pill-shaped action triggers, and fluid spring morphing transitions). 

Currently, SyncLine functions as a **high-fidelity, fully interactive frontend prototype**. All business entities—including user profiles, clients, conversation history, projects, tasks, meetings, invoices, files, and notifications—are backed by static mock data defined in `MockDataProvider.kt` and held in-memory via `GlobalChatViewModel.kt`. 

### Key Audit Findings:
* **UI & Animation Fidelity:** Outstanding UI polish with custom morphing bottom sheet navigation (`BottomNavBar.kt`), interactive invoice payment animations with checkmarks, quick action pill menus, and real-time timezone calculation engines (`TimezoneEngine.kt`).
* **Data Persistence State:** **100% In-Memory / Temporary.** Any changes made during a session (e.g., sending messages, adding projects/invoices, scheduling meetings, marking invoices as paid, marking notifications read) are stored in `StateFlow` streams in RAM. Restarting or recreating the application process resets all state to default mock data.
* **Authentication State:** **Simulated.** `LoginScreen.kt` provides user interface forms for email/password sign-in and Google OAuth, but clicking "Sign In" simply bypasses credential checks and navigates to the main app dashboard.
* **Backend Readiness:** `build.gradle.kts` includes initial dependency declarations for Firebase (BOM, Firebase AI, App Check), Retrofit, Moshi, and Room, but no network repositories, Firebase Auth instances, Firestore data access objects, or Realtime Database schemas have been implemented yet.

---

## 2. TECHNICAL INVENTORY & REPOSITORY MAP

### 2.1 Technical Stack & Dependency Inventory
* **Language:** Kotlin `2.0.21`
* **UI Framework:** Jetpack Compose (Compose BOM `2024.09.00`, Material 3)
* **Build System:** Gradle 8.x with Kotlin DSL (`build.gradle.kts`), Version Catalog (`gradle/libs.versions.toml`)
* **Architecture Pattern:** Single-Activity MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
* **Asynchronous Operations:** Kotlin Coroutines (`1.9.0`) & `StateFlow` / `MutableStateFlow`
* **Navigation:** Navigation Compose (`2.8.2`) with custom tab state orchestration and `BackHandler` support
* **Timezone Engine:** Java 8+ `java.time` API (`Instant`, `ZoneId`, `ZonedDateTime`, `DateTimeFormatter`)

### 2.2 Directory & Repository Structure Map
```
/app/src/main/java/com/example/
├── MainActivity.kt                      # Application entry point, theme setup, root NavGraph host
├── components/
│   ├── BottomNavBar.kt                 # Monolithic morphing bottom navbar (4,720+ lines), pill sheets, search sheet
│   ├── CreateBottomSheet.kt            # Central "+" action trigger sheet (New Chat, Project, Schedule)
│   ├── ShowcaseDebugDrawer.kt          # Developer debug screen switcher dialog
│   └── WiseComponents.kt               # Custom M3 UI widgets (WiseButton, WiseCard, WiseAvatar, WiseHeader)
├── data/
│   └── MockDataProvider.kt             # Static repository of clients, messages, projects, invoices, meetings, cities
├── engine/
│   └── TimezoneEngine.kt               # IANA timezone conversion, working hour overlaps, daylight savings helper
├── model/
│   └── Models.kt                       # Domain data classes (Client, Message, Project, Invoice, Meeting, etc.)
├── navigation/
│   ├── GlobalChatNavGraph.kt           # NavHost routes, screen composable mappings, action pill payload parsing
│   └── Screen.kt                       # Sealed class route definitions ("splash", "login", "main", "chat/{id}", etc.)
├── screens/                            # 19 Screen composables
│   ├── AssistantScreen.kt              # AI assistant placeholder screen
│   ├── CalendarScreen.kt               # Upcoming meetings & schedule list screen
│   ├── ChatScreen.kt                   # Interactive chat conversation view, cards, payment morph buttons
│   ├── ClientDetailsScreen.kt          # Client profile, timezone status, working hours, schedule action
│   ├── ContactsScreen.kt               # Client contact directory screen
│   ├── EmptyStateScreen.kt             # Empty inbox / zero-state placeholder
│   ├── FilesScreen.kt                  # Shared project files, attachments, filterable list
│   ├── HomeScreen.kt                   # Executive overview dashboard (inbox preview, active projects, timezones)
│   ├── InboxScreen.kt                  # Filterable chat inbox list (All, Unread, Starred)
│   ├── LoginScreen.kt                  # Authentication screen UI
│   ├── MoreMenuScreen.kt               # Additional settings/navigation drawer
│   ├── NotificationsScreen.kt          # Activity feed and notifications screen
│   ├── ProjectDetailsScreen.kt         # Project breakdown, interactive tasks, project files
│   ├── ProjectsScreen.kt               # Active/Completed project list view
│   ├── ScheduleMessageScreen.kt        # Timezone-aware message scheduling form
│   ├── SearchScreen.kt                 # Fullscreen global search view
│   ├── SettingsScreen.kt               # Account settings, theme toggles, logout
│   ├── SplashScreen.kt                # Onboarding splash screen with brand headline
│   └── TimezonesScreen.kt              # Global clock & timezone status overview
├── ui/theme/
│   ├── Color.kt                        # Wise design color definitions (WiseLime, DarkGreen, AppBackground)
│   ├── Theme.kt                        # Custom CompositionLocal provider (GlobalChatCustomColors) and M3 setup
│   └── Type.kt                         # Typography scale with Inter and Wise Sans Black heavy weight
└── viewmodel/
    └── GlobalChatViewModel.kt          # Central ViewModel managing state flows for all screens
```

---

## 3. COMPLETE SCREEN INVENTORY

| # | Screen Name | Route Pattern | File Location | Primary Purpose | Navigation Trigger |
|---|---|---|---|---|---|
| 1 | Splash | `splash` | `screens/SplashScreen.kt` | Brand introduction & onboarding call to action | Application launch start destination |
| 2 | Login | `login` | `screens/LoginScreen.kt` | User authentication form (Email/Password & Google) | Splash "Sign In" button or Settings logout |
| 3 | Home (Overview) | `main` (Tab: HOME) | `screens/HomeScreen.kt` | Executive dashboard showing inbox preview, active projects, upcoming meetings & global timezones | Bottom Nav "Home" tab |
| 4 | Inbox | `main` (Tab: CHAT) | `screens/InboxScreen.kt` | Overview of all active conversations with unread badges, filterable tabs (All, Unread, Starred) | Bottom Nav "Chat" tab |
| 5 | Chat Conversation | `chat/{clientId}` | `screens/ChatScreen.kt` | Real-time conversation view with interactive invoice cards, project attachments, file downloads, and morphing bottom navbar composer | Tapping an inbox item or client card |
| 6 | Client Details | `client_details/{clientId}` | `screens/ClientDetailsScreen.kt` | Detailed client profile, location, local time, working hours overlap, and message scheduling trigger | Tapping client avatar/header in chat or home |
| 7 | Projects Overview | `main` (Sub-view) | `screens/ProjectsScreen.kt` | Tabbed view of active, completed, and all project cards with progress bars | Home "View All Projects" |
| 8 | Project Details | `project_details/{projectId}` | `screens/ProjectDetailsScreen.kt` | Detailed breakdown of project tasks, completion percentages, project files, and linked client chat button | Tapping any project card |
| 9 | Files & Attachments | `files` | `screens/FilesScreen.kt` | Centralized file manager displaying PDF, Figma, DOCX, ZIP files with category filters | Home "Files" shortcut |
| 10 | Schedule Message | `schedule_message/{clientId}` | `screens/ScheduleMessageScreen.kt` | Timezone-aware scheduling tool allowing users to pick future delivery date/time | Client Details or "+" Create sheet |
| 11 | Calendar & Meetings | `calendar` | `screens/CalendarScreen.kt` | Schedule view of upcoming client meetings and design reviews | Home "Calendar" card |
| 12 | Activity / Notifications | `main` (Tab: ACTIVITY) | `screens/NotificationsScreen.kt` | System notifications, message alerts, and payout approvals feed | Bottom Nav "Activity" tab |
| 13 | Search | `search` | `screens/SearchScreen.kt` | Global search screen filtering people, actions, projects, files, and invoices | Top search icons or bottom navbar search |
| 14 | Account & Settings | `main` (Tab: ACCOUNT) | `screens/SettingsScreen.kt` | Profile management, theme toggles, notifications preferences, and sign out | Bottom Nav "Account" tab |
| 15 | Global Timezones | `timezones` | `screens/TimezonesScreen.kt` | Multi-city clock grid comparing local times across London, Tokyo, Berlin, NY, New Delhi, Sydney | Home "Timezones" header |
| 16 | Contacts | `contacts` | `screens/ContactsScreen.kt` | Client contact directory | More menu directory shortcut |
| 17 | AI Assistant | `assistant` | `screens/AssistantScreen.kt` | Conversational AI workspace placeholder | Top bar assistant shortcut |
| 18 | Empty State | `empty_state` | `screens/EmptyStateScreen.kt` | Zero-data placeholder view for new accounts | Navigation fallback |
| 19 | Developer Debug Showcase | Dialog | `components/ShowcaseDebugDrawer.kt` | Screen preview switcher for developer QA | ViewModel debug trigger |

---

## 4. UI & INTERACTION CATALOGUE

| Screen | Element | User Action | Expected Result | Current Implementation | Source File | Backend Requirement |
|---|---|---|---|---|---|---|
| Splash | "Get Started" Button | Click | Navigate to Main screen | Calls `onGetStarted()` callback | `SplashScreen.kt` | None |
| Splash | "Sign In" Link | Click | Navigate to Login screen | Calls `onSignIn()` callback | `SplashScreen.kt` | None |
| Login | Email & Password fields | Type text | Updates local text state | Local `remember { mutableStateOf("") }` | `LoginScreen.kt` | Firebase Auth validation |
| Login | "Sign In" Button | Click | Authenticate user & open Main screen | Invokes `onSignInSuccess()` without checking credentials | `LoginScreen.kt` | Firebase `signInWithEmailAndPassword` |
| Login | "Google Sign In" Button | Click | Authenticate with Google | Dummy callback trigger | `LoginScreen.kt` | Jetpack Credential Manager & `GetSignInWithGoogleOption` |
| BottomNavBar | Bottom Tabs (Home, Chat, Activity, Account) | Click | Switch main view tab | Updates `currentBottomTab` state in `GlobalChatNavGraph.kt` | `BottomNavBar.kt` | None (Local Navigation) |
| BottomNavBar | "+" Plus Pill Button | Click | Open Create Bottom Sheet | Sets `isCreateSheetOpen = true` | `BottomNavBar.kt` | None |
| BottomNavBar | Attach Pill (Chat Mode) | Click | Expand file/media/link attachment menu | Toggles `activePillSheet` state to `"attach"` | `BottomNavBar.kt` | Google Drive / Firebase Storage chooser |
| BottomNavBar | Attach -> Document | Click | Pick document file from device | Triggers `rememberLauncherForActivityResult(GetContent)` | `BottomNavBar.kt` | Upload document file to Storage |
| BottomNavBar | Attach -> Photo & Video | Click | Pick photo from device gallery | Triggers `PickVisualMedia()` visual picker | `BottomNavBar.kt` | Upload image file to Storage |
| BottomNavBar | Attach -> Share Link | Click | Open link sharing title/URL form sheet | Morphs menu to link input sheet | `BottomNavBar.kt` | Store link record in Firestore |
| BottomNavBar | Attach -> Recent Files | Click | Pick file from recent uploads list | Sends file attachment payload to chat | `BottomNavBar.kt` | Fetch user files from Firestore |
| BottomNavBar | Project Pill (Chat Mode) | Click | Open project creation/selection sheet | Morphs menu to project creation form | `BottomNavBar.kt` | Create project record in Firestore |
| BottomNavBar | Schedule Pill (Chat Mode) | Click | Open meeting scheduling sheet | Morphs menu to meeting form | `BottomNavBar.kt` | Create meeting record in Firestore |
| BottomNavBar | Invoice Pill (Chat Mode) | Click | Open invoice creation form sheet | Morphs menu to invoice creation form | `BottomNavBar.kt` | Create invoice record in Firestore |
| BottomNavBar | Search Input (Chat Mode) | Click & Type | Auto-morph navigation bar height & filter items | Dynamic spring height calculation based on `searchResultCount` | `BottomNavBar.kt` | Firestore search query |
| Chat | "Mark as Paid" Button | Click | Update invoice status to paid, trigger checkmark spring animation | Calls `viewModel.markInvoicePaid(id)` | `ChatScreen.kt` | Firestore `update("isPaid", true)` |
| ProjectDetails | Task Checkbox | Click | Toggle task completed state & re-calculate progress % | Calls `viewModel.toggleTask(projectId, taskId)` | `ProjectDetailsScreen.kt` | Firestore task state update |
| Settings | Dark Theme Toggle | Click | Switch global app color theme | Calls `viewModel.toggleTheme()` | `SettingsScreen.kt` | Local DataStore preference |

---

## 5. WORKFLOW CATALOGUE & COMPLETION MATRIX

### 5.1 Workflow Catalog
1. **User Authentication Flow:**
   - *Status:* **Mocked (UI Only)**.
   - *Path:* `SplashScreen.kt` -> `LoginScreen.kt` -> `GlobalChatNavGraph.kt`.
   - *Behavior:* Form inputs accepted; clicking "Sign In" immediately navigates to `Main` route without backend identity verification or session token creation.
2. **Sending Text Chat Messages:**
   - *Status:* **In-Memory Persistent (Session Only)**.
   - *Path:* `BottomNavBar.kt` (Chat Mode composer) -> `GlobalChatViewModel.sendMessage()` -> `ChatScreen.kt`.
   - *Behavior:* Appends new `Message` object to `_messages` flow, updates `_inboxItems` preview. Visible instantly in UI; lost upon app restart.
3. **Attaching Documents & Media:**
   - *Status:* **Partially Implemented**.
   - *Path:* `BottomNavBar.kt` (Attach menu) -> Android System File Picker / Photo Picker -> `viewModel.addFileAttachmentAndSendToChat()`.
   - *Behavior:* Uses Android native `PickVisualMedia` and `GetContent` contracts to read local file URI and file size, then formats a local `FileAttachment` card message. File binary is NOT uploaded to any server.
4. **Sharing Links:**
   - *Status:* **In-Memory Persistent**.
   - *Path:* `BottomNavBar.kt` (Attach -> Share Link) -> Title & URL input -> `viewModel.sendLinkToChat()`.
   - *Behavior:* Formats link message string (`🔗 Title \n URL`) and appends to chat.
5. **Creating Projects from Chat:**
   - *Status:* **In-Memory Persistent**.
   - *Path:* `BottomNavBar.kt` (Project pill) -> Form input -> `viewModel.addProjectAndSendToChat()`.
   - *Behavior:* Creates a new `Project` object, appends it to `_projects` flow, and sends a rich `ProjectCard` bubble to the chat conversation.
6. **Project Task Management:**
   - *Status:* **In-Memory Persistent**.
   - *Path:* `ProjectDetailsScreen.kt` -> Click Task Checkbox -> `viewModel.toggleTask()`.
   - *Behavior:* Flips boolean state of task, recalculates progress percentage (`0..100`), updates progress bar.
7. **Creating & Sending Invoices:**
   - *Status:* **In-Memory Persistent**.
   - *Path:* `BottomNavBar.kt` (Invoice pill) -> Form input -> `viewModel.addInvoiceAndSendToChat()`.
   - *Behavior:* Generates invoice ID (`INV-2024-00X`), appends to `_invoices` flow, and posts a `LargeInvoiceCardBubble` to chat.
8. **Marking Invoices as Paid:**
   - *Status:* **In-Memory Persistent**.
   - *Path:* `ChatScreen.kt` (Receiver side) -> Click "Mark as Paid" -> Morph button & checkmark spring animation -> `viewModel.markInvoicePaid()`.
   - *Behavior:* Updates invoice status to `isPaid = true`, formats paid timestamp (`"Paid on MMM d, yyyy at h:mm a"`), and updates state in both messages and invoice flows.
9. **Scheduling Meetings:**
   - *Status:* **In-Memory Persistent**.
   - *Path:* `BottomNavBar.kt` (Schedule pill) or `ScheduleMessageScreen.kt` -> `viewModel.addMeetingAndSendToChat()`.
   - *Behavior:* Appends `Meeting` object to `_meetings` flow and posts `MeetingCard` bubble in chat.

---

## 6. MOCK DATA & PERSISTENCE MIGRATION MAP

| Entity | Current Source | Read Locations | Write Locations | Current Limitations | Future Persistence Target |
|---|---|---|---|---|---|
| **User Profile / Auth** | Hardcoded ("You" / `c1`) | `ChatScreen.kt`, `SettingsScreen.kt` | `LoginScreen.kt` (Dummy) | No real user IDs, authentication tokens, or multi-user session state | **Firebase Auth** (Google Sign-In + Email/Password) |
| **Clients / Contacts** | `MockDataProvider.clients` | `HomeScreen.kt`, `InboxScreen.kt`, `ClientDetailsScreen.kt` | None (Static) | Client list is hardcoded; cannot add or edit clients | **Cloud Firestore** (`clients` collection) |
| **Messages / History** | `MockDataProvider.getInitialMessages()` | `ChatScreen.kt`, `InboxScreen.kt` | `GlobalChatViewModel.sendMessage()` | Lost on app restart; single-device local RAM only | **Firebase Realtime Database** (Relay) + **Room DB** (Local Store) |
| **Projects** | `MockDataProvider.initialProjects` | `HomeScreen.kt`, `ProjectsScreen.kt`, `ProjectDetailsScreen.kt` | `GlobalChatViewModel.addProjectAndSendToChat()` | Resets on restart; progress calculations in-memory only | **Cloud Firestore** (`projects` collection) |
| **Tasks** | Embedded in `Project.tasks` | `ProjectDetailsScreen.kt` | `GlobalChatViewModel.toggleTask()` | Task state resets on restart | **Cloud Firestore** (`projects/{id}/tasks` subcollection) |
| **Invoices** | `MockDataProvider.initialPastInvoices` | `ChatScreen.kt`, `InboxScreen.kt` | `viewModel.addInvoiceAndSendToChat()`, `viewModel.markInvoicePaid()` | Resets on restart; no PDF file generated or stored | **Cloud Firestore** (`invoices` collection) |
| **Meetings** | `MockDataProvider.calendarMeetings` | `CalendarScreen.kt`, `ChatScreen.kt` | `viewModel.addMeetingAndSendToChat()` | Resets on restart | **Cloud Firestore** (`meetings` collection) |
| **File Attachments** | `MockDataProvider.allFiles` | `FilesScreen.kt`, `ChatScreen.kt` | `viewModel.addFileAttachmentAndSendToChat()` | URIs point to temporary local device files; no remote cloud storage | **Google Drive API** (User-owned) / **Firebase Storage** |
| **Notifications** | `MockDataProvider.initialNotifications` | `NotificationsScreen.kt`, `HomeScreen.kt` | `viewModel.markAllNotificationsRead()` | Resets on restart | **Cloud Firestore** (`users/{id}/notifications`) |
| **Theme & Preferences**| `GlobalChatViewModel._isDarkTheme` | `MainActivity.kt`, `SettingsScreen.kt` | `viewModel.toggleTheme()` | Theme choice lost on restart | **Jetpack DataStore Preferences** |

---

## 7. CURRENT ARCHITECTURE DIAGRAM (PLAIN TEXT)

```
[ User Interaction ]
         │
         ▼
[ Jetpack Compose UI Layer ]
  ├── SplashScreen / LoginScreen
  ├── Main Scaffold Host (GlobalChatNavGraph)
  │     ├── HomeScreen (Dashboard Overview)
  │     ├── InboxScreen / ChatScreen (Conversations & Cards)
  │     ├── ProjectsScreen / ProjectDetailsScreen
  │     ├── FilesScreen / CalendarScreen / SettingsScreen
  │     └── BottomNavBar (Pill Menus, Morphing Composer, Search)
         │
         ▼ (Unidirectional Data Flow / StateFlow Collection)
[ GlobalChatViewModel ] ─── (TimezoneEngine.kt for UTC/IANA logic)
  ├── _isDarkTheme : StateFlow<Boolean>
  ├── _clients : StateFlow<List<Client>>
  ├── _messages : StateFlow<List<Message>>
  ├── _projects : StateFlow<List<Project>>
  ├── _invoices : StateFlow<List<Invoice>>
  ├── _meetings : StateFlow<List<Meeting>>
  ├── _files : StateFlow<List<FileAttachment>>
  └── _notifications : StateFlow<List<NotificationItem>>
         │
         ▼ (Initial Memory Seeding)
[ MockDataProvider.kt ] (Static Hardcoded Kotlin Data Objects)
```

---

## 8. PROPOSED FIREBASE & CLOUD BACKEND ARCHITECTURE

To transition SyncLine from a prototype into a production-ready, multi-user application while preserving every existing visual transition and design behavior, the following backend architecture is recommended:

```
                                 ┌──────────────────────────┐
                                 │ Firebase Authentication  │
                                 │  (Google Sign-In & Auth) │
                                 └────────────┬─────────────┘
                                              │ UID & Identity Tokens
                                              ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                    SyncLine App Engine                                 │
│                                                                                        │
│  ┌───────────────────────┐   ┌───────────────────────────┐   ┌──────────────────────┐  │
│  │ Firebase Realtime DB  │   │      Room Local Database  │   │  Cloud Firestore DB  │  │
│  │ (Ephemeral Chat Relay)│──►│ (Durable Encrypted Local  │   │  (Durable Business   │  │
│  │                       │   │     Chat & Message Store) │   │   Records & State)   │  │
│  └───────────────────────┘   └───────────────────────────┘   └──────────────────────┘  │
│              │                            │                              │             │
└──────────────┼────────────────────────────┼──────────────────────────────┼─────────────┘
               │                            │                              │
               ▼                            ▼                              ▼
      [ Live Chat Delivery ]      [ Instant Offline Search ]     [ Projects, Invoices,  ]
      [ Temporary Queue    ]      [ & Local Chat History   ]     [ Meetings & Profiles  ]
                                                                           │
                                                                           ▼
                                                               ┌────────────────────────┐
                                                               │    Google Drive API    │
                                                               │ (User Files & Media)   │
                                                               └────────────────────────┘
```

### 8.1 Component Breakdown

1. **Firebase Authentication:**
   - Authenticate users via **Google Sign-In** (`CredentialManager` API with `GetSignInWithGoogleOption`) and Email/Password.
   - User document initialized in Firestore upon first registration.

2. **Cloud Firestore (Durable Business Data):**
   - Stores non-ephemeral records:
     - `users/{userId}`: User profiles, timezone settings, working hours.
     - `clients/{clientId}`: Client company info, country codes, timezone IANA IDs.
     - `projects/{projectId}`: Title, client, due date, progress %, status (`IN_PROGRESS`, `COMPLETED`), with subcollection `tasks/{taskId}`.
     - `invoices/{invoiceId}`: Title, amount, currency, due date, payment status (`isPaid`), paid timestamp string.
     - `meetings/{meetingId}`: Title, client, time, duration, date, notes, status.
     - `notifications/{notificationId}`: Type, title, sender, timestamp, read status.

3. **Firebase Realtime Database (RTDB) — Ephemeral Chat Relay:**
   - Realtime Database used strictly as a **temporary message transport queue** for low-latency live messaging between devices.
   - **Delivery Flow:**
     1. Sender writes message to `/chat_relay/{conversationId}/{messageId}`.
     2. Recipient receives payload via real-time RTDB listener.
     3. Recipient writes payload to local Room database.
     4. Recipient sends ACK back to RTDB.
     5. RTDB removes temporary relay message after ACK or timeout expiration.

4. **Room Database (Local Durable Chat History):**
   - Keeps long-term chat message history local to the user's device for maximum privacy, zero-latency rendering, and offline search capabilities.
   - Entities: `MessageEntity`, `AttachmentEntity`, `ConversationEntity`.

5. **Google Drive API (File Attachments):**
   - Integrated via Google OAuth scope (`https://www.googleapis.com/auth/drive.file`).
   - Shared documents and photos stored in user's Google Drive; file references (Drive file ID, view URL, size) passed in chat messages.

---

## 9. SECURITY & PRIVACY REVIEW

### 9.1 Vulnerabilities & Missing Access Controls in Prototype
1. **Unauthenticated Access:** Anyone can view all mock client conversations, projects, and invoices without signing in.
2. **Missing Authorization Rules:** Client actions (such as marking an invoice as paid) currently run locally in RAM without checking whether the active user has authority to modify that invoice.
3. **Insecure Storage of Tokens / API Keys:** Currently, no credentials exist in code, but future integrations must inject keys securely via `BuildConfig` using `.env` files (Secrets Gradle Plugin) rather than hardcoding.

### 9.2 Conceptual Firebase Security Rules

#### Firestore Security Rules (`firestore.rules`):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper Functions
    function isAuthenticated() {
      return request.auth != null;
    }
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    function isParticipant(participants) {
      return isAuthenticated() && (request.auth.uid in participants);
    }

    // User Profiles
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if isOwner(userId);
    }

    // Projects
    match /projects/{projectId} {
      allow read, write: if isAuthenticated() && (
        resource == null || request.auth.uid in resource.data.participantUids
      );
    }

    // Invoices
    match /invoices/{invoiceId} {
      allow read: if isAuthenticated() && (
        request.auth.uid == resource.data.issuerUid || request.auth.uid == resource.data.clientUid
      );
      // Only the client/receiver can mark invoice as paid
      allow update: if isAuthenticated() && request.auth.uid == resource.data.clientUid
        && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['isPaid', 'paidAt']);
      allow create: if isAuthenticated();
    }
  }
}
```

#### Realtime Database Rules (`database.rules.json`):
```json
{
  "rules": {
    "chat_relay": {
      "$conversationId": {
        ".read": "auth != null && root.child('conversations').child($conversationId).child('participants').child(auth.uid).exists()",
        ".write": "auth != null && root.child('conversations').child($conversationId).child('participants').child(auth.uid).exists()"
      }
    }
  }
}
```

---

## 10. COST & SCALABILITY REVIEW (100 Active Users)

### 10.1 Expected Resource Consumption & Free Tier Evaluation

| Resource / Service | Free Tier Allowance | Estimated Usage (100 Users) | Cost Impact |
|---|---|---|---|
| **Firebase Auth** | Unlimited (Email & Google) | ~100 MAU | **$0.00 / Free** |
| **Cloud Firestore Reads** | 50,000 / day | ~5,000 / day | **$0.00 / Free** |
| **Cloud Firestore Writes** | 20,000 / day | ~1,200 / day | **$0.00 / Free** |
| **Cloud Firestore Storage** | 1 GiB total | ~150 MB total | **$0.00 / Free** |
| **Realtime DB Connections** | 100 simultaneous connections | ~25-40 peak concurrent | **$0.00 / Free** |
| **Realtime DB Download** | 10 GB / month | ~500 MB / month | **$0.00 / Free** |
| **Google Drive API** | 1,000,000 requests / day | ~2,000 requests / day | **$0.00 / Free** |

**Optimization Strategy:** By storing chat history locally in Room and using RTDB only for active message relay, Firestore document reads for chat scrollbacks are eliminated completely, keeping usage well within free tiers.

---

## 11. TESTING & ACCEPTANCE PLAN

### 11.1 Test Matrix

1. **Authentication Unit & Integration Tests:**
   - Verify user registration, Google OAuth login state propagation, and token persistence.
2. **Room Database Unit Tests (Robolectric):**
   - Verify `MessageDao` insertion, retrieval by conversation ID, duplicate message suppression, and deletion.
3. **Firestore Security Rule Tests (Firebase Emulator Suite):**
   - Test unauthorized read/write attempts to invoices and projects from non-participant accounts.
4. **Realtime Message Delivery Integration Tests:**
   - Verify two distinct user sessions sending messages over RTDB relay and receiving ACKs.
5. **Invoice Morph & Payment State Tests:**
   - Verify clicking "Mark as Paid" correctly updates status in Firestore and animates checkmark spring without UI regression.
6. **Timezone Engine Tests:**
   - Verify working hours overlap calculation across GMT, EST, JST, IST, and AEDT time zones.

---

## 12. STEP-BY-STEP MIGRATION PLAN

### Phase 1: Authentication & User Management
- Enable Firebase Auth in project.
- Connect `LoginScreen.kt` to `FirebaseAuth` with Google Sign-In via `CredentialManager`.
- Store user profiles in Firestore (`/users/{uid}`).

### Phase 2: Firestore Business Records Setup
- Create `FirestoreRepository` handling `projects`, `invoices`, `meetings`, and `clients`.
- Connect `ProjectsScreen`, `ProjectDetailsScreen`, `CalendarScreen`, and `FilesScreen` to real-time Firestore snapshots.

### Phase 3: Room Database & RTDB Ephemeral Chat Integration
- Create Room database schema for local message storage (`MessageEntity`).
- Implement RTDB message relay engine in `GlobalChatViewModel`.
- Update `ChatScreen` and `BottomNavBar` composer to post messages to RTDB relay and observe local Room DB.

### Phase 4: Google Drive File Attachment Integration
- Configure Google Drive OAuth scope.
- Connect `BottomNavBar.kt` file picker to upload media to user's Drive and attach file links in chat.

---

## 13. QUESTIONS REQUIRING USER DECISION

1. **Chat History Backup:** Should chat history remain strictly local on device (maximum privacy & zero cloud cost), or should we implement an optional encrypted Firestore backup for chat history?
2. **Multi-User Client Relationships:** Should clients be globally shared contacts across all workspace users, or private to each user's account?
3. **Google Drive Storage Scope:** Do you prefer using Google Drive (`https://www.googleapis.com/auth/drive.file` - user-owned storage) or Firebase Storage for file attachments?

---

## 14. AUDIT LIMITATIONS

* **No Remote Backend Provisioning Performed:** As instructed by the non-negotiable audit rules, no external cloud resources, Firebase databases, or Google Cloud OAuth client IDs were provisioned during this discovery phase.
* **Device Hardware Testing:** Camera hardware capture was audited via static contract definitions (`PickVisualMedia` and `GetContent`) as no physical Android camera hardware exists in the cloud execution environment.
