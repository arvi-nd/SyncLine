# GlobalChat — Confirmed Implementation Plan

Rebuild the complete 18-screen global freelancer workspace in native Android using Jetpack Compose, faithfully reproducing the visual hierarchy, Wise-inspired color and typography palette, and smooth interactions depicted in the reference showcase.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions are confirmed and authoritative:
> - **Bottom Navigation**: Chats · Contacts · Central Plus · Projects · More. The central Plus button is an action that opens the Create Sheet (New Chat, New Project, Schedule Message). More provides access to Timezones, Calendar, Files, Notifications, and Settings.
> - **Launch Flow**: Splash → Login / Sign Up → Inbox. Frontend mock demonstration with smooth backstack handling.
> - **Mock Data Persistence**: Strictly in-memory. Resets when the app process restarts. No SQLite, Room, DataStore, or remote storage.
> - **Simulated AI Assistant Screen**: Purely visual layout matching the reference (summary card, quick action chips, composer), with actions inactive using clear disabled styling. No fake AI generation claims.
> - **Dual Theme System**: Full Light and Dark themes. Light theme strictly matches the supplied Wise-inspired reference (`#9FE870` lime, `#163300` dark green, `#0E0F0C` text, `#F7F7EF` warm off-white, `#E2F6D5` mint, white cards with 1dp border). Dark theme uses deep neutral dark surfaces (`#121411`, `#1C1F1A`) while maintaining high-contrast hierarchy and brand lime accents.
> - **Time-Zone Engine**: `java.time` with explicit IANA identifiers (`America/New_York`, `Europe/London`, `Europe/Berlin`, `Asia/Kolkata`, `Australia/Sydney`), testable overlap math, and working hours calculation.
> - **Debug-Only Showcase Switcher**: An isolated developer drawer for fast visual inspection of all 18 screens without altering production navigation.

---

## 1. Overview & Architecture

```
┌────────────────────────────────────────────────────────┐
│                   GlobalChatApp                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │                  Navigation Host                   │ │
│ │  ┌───────────────┬──────────────────────────────┐  │ │
│ │  │ Onboarding    │ Main Scaffold With BottomNav │  │ │
│ │  │ • Splash      │ • Inbox (Chats)              │  │ │
│ │  │ • Auth / Login│ • Contacts                   │  │ │
│ │  │               │ • Projects                   │  │ │
│ │  │               │ • More (Timezones/Cal/Set)   │  │ │
│ │  └───────────────┴──────────────────────────────┘  │ │
│ │  ┌──────────────────────────────────────────────┐  │ │
│ │  │ Modal Sheets & Sub-Screens                   │  │ │
│ │  │ • ChatScreen           • ProjectDetails      │  │ │
│ │  │ • ClientDetails        • ScheduleMessage     │  │ │
│ │  │ • FilesScreen          • SearchScreen        │  │ │
│ │  │ • NotificationsScreen  • AIAssistantScreen   │  │ │
│ │  │ • CreateActionSheet    • EmptyStatePreview   │  │ │
│ │  └──────────────────────────────────────────────┘  │ │
│ └────────────────────────────────────────────────────┘ │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Debug-Only Showcase Switcher (Development Overlay) │ │
│ └────────────────────────────────────────────────────┘ │
└───────────────────────────┬────────────────────────────┘
                            │
               ┌────────────┴────────────┐
               ▼                         ▼
    ┌───────────────────────┐ ┌──────────────────────┐
    │  GlobalChatViewModel  │ │  GlobalChatTheme     │
    │  • In-Memory State    │ │  • Light & Dark M3   │
    │  • Timezone Engine    │ │  • Wise Color Tokens │
    │  • Deterministic Mock │ │  • Typography & Shapes│
    └───────────────────────┘ └──────────────────────┘
```

## 2. All 18 Screens Checklist

1. **Splash / Launch**: Full-bleed `#9FE870` lime, bold billboard headline ("Work beyond borders. Chat. Collaborate. Get paid. With clients worldwide."), minimalist wireframe globe, and "Get Started" pill button.
2. **Login / Sign Up**: "Welcome back", email & password with visibility toggle, "Sign In" button, divider, Google and Apple sign-in options, and sign-up toggle.
3. **Home / Inbox**: Header with "GlobalChat", search & add icons, filter chips (`All`, `Unread 3`, `Starred`), conversation list with avatars, unread indicators, timestamps, and client snippets.
4. **Chat Screen**: Header for Sarah Johnson with local time & call actions, message bubbles (incoming white/light-gray, outgoing lime `#9FE870`), PDF attachment preview card, input field with attachment icon and send button.
5. **Client Details / Timezone**: Sarah Johnson profile, role, online badge, current local time (`2:24 PM EST`), working hours (`9:00 AM - 6:00 PM`), availability tag, time-zone overlap card (`2h 30m overlap`), and "Schedule message" button.
6. **Global Timezones**: City cards (New York, London, Berlin, New Delhi, Sydney) with live local clocks, UTC offsets, day/night sun/moon icons, and working overlap indicator.
7. **Contacts**: Search field, add button, contact rows with country flags, local times, and profile navigation.
8. **Projects**: Filter tabs (`Active`, `Completed`, `All`), project progress cards (Mobile Redesign, Brand Strategy, Social Media Kit) with progress bars, due dates, and status chips.
9. **Project Details**: Hero title, client tag, deadline, interactive task checklist, file attachments, and "View chat" button.
10. **Files & Attachments**: Category chips (`All`, `Shared`, `My Files`), file list (PDF, DOCX, ZIP, TXT, Folders) with metadata and download icons.
11. **Schedule Message**: Recipient selector, message text, date & time picker, dual time-zone conversion context, and "Schedule" button.
12. **Calendar / Meetings**: Date picker carousel, daily meeting agenda (Client Call, Design Review, Team Check-in) with start times and duration.
13. **Settings**: Profile avatar (Alex Carter), appearance toggle (Light / Dark theme), working hours, notifications, privacy, and support.
14. **Search**: Search field with instant query filtering across Clients, Messages, Projects, and Files.
15. **Notifications**: Chronological activity feed with unread indicators and "Mark all read" action.
16. **Messages / AI Assistant**: Visual-only assistant layout with "Catch me up" summary card, quick action chips (Summarize, Draft a reply, Translate), and composer with inactive disabled styling.
17. **Empty States**: Friendly illustration for "No messages yet", explanatory copy, and "New message" action.
18. **Mobile Navigation & Create Sheet**: Persistent M3 bottom navigation (Chats, Contacts, Projects, More) with prominent central lime `+` action opening the floating Create Sheet (New chat, New project, Schedule message).
