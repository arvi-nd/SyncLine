const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ORGANIZER_UID = "organizer_uid_100";
const PARTICIPANT_UID = "participant_uid_200";
const UNAUTHORIZED_UID = "intruder_uid_300";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function getValidMeetingData(overrides = {}) {
  return {
    id: "m_test_123",
    title: "Sprint Review & Planning",
    clientName: "Acme Corp",
    organizerUid: ORGANIZER_UID,
    participantUids: [ORGANIZER_UID, PARTICIPANT_UID],
    durationMinutes: 30,
    status: "Confirmed",
    duration: "30 min",
    time: "10:00 AM",
    dateString: "Nov 15, 2026",
    isOnline: true,
    description: "Review deliverables and discuss milestones",
    ...overrides,
  };
}

// ==========================================
// 1. UNAUTHENTICATED SCENARIOS
// ==========================================
test("Unauthenticated user: cannot read meetings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("meetings").doc("m_test_123").get());
});

test("Unauthenticated user: cannot create meetings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("meetings").doc("m_test_123").set(getValidMeetingData())
  );
});

// ==========================================
// 2. CREATION SCENARIOS & SCHEMA VALIDATION
// ==========================================
test("Organizer: can create valid meeting when organizerUid matches auth and is in participants", async () => {
  const organizerDb = testEnv.authenticatedContext(ORGANIZER_UID).firestore();
  await assertSucceeds(
    organizerDb.collection("meetings").doc("m_test_123").set(getValidMeetingData())
  );
});

test("Organizer: cannot create meeting with someone else as organizerUid", async () => {
  const organizerDb = testEnv.authenticatedContext(ORGANIZER_UID).firestore();
  await assertFails(
    organizerDb.collection("meetings").doc("m_test_123").set(
      getValidMeetingData({ organizerUid: UNAUTHORIZED_UID })
    )
  );
});

test("Organizer: cannot create meeting if organizerUid is not in participantUids", async () => {
  const organizerDb = testEnv.authenticatedContext(ORGANIZER_UID).firestore();
  await assertFails(
    organizerDb.collection("meetings").doc("m_test_123").set(
      getValidMeetingData({ participantUids: [PARTICIPANT_UID] })
    )
  );
});

test("Creation rejection: missing required title or empty title", async () => {
  const organizerDb = testEnv.authenticatedContext(ORGANIZER_UID).firestore();
  await assertFails(
    organizerDb.collection("meetings").doc("m_test_123").set(
      getValidMeetingData({ title: "" })
    )
  );
});

test("Creation rejection: invalid durationMinutes (not in allowed list)", async () => {
  const organizerDb = testEnv.authenticatedContext(ORGANIZER_UID).firestore();
  await assertFails(
    organizerDb.collection("meetings").doc("m_test_123").set(
      getValidMeetingData({ durationMinutes: 999 })
    )
  );
});

test("Creation rejection: invalid status", async () => {
  const organizerDb = testEnv.authenticatedContext(ORGANIZER_UID).firestore();
  await assertFails(
    organizerDb.collection("meetings").doc("m_test_123").set(
      getValidMeetingData({ status: "InvalidStatusXYZ" })
    )
  );
});

// ==========================================
// 3. READ SCENARIOS
// ==========================================
test("Authorized participant: can read scheduled meeting", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const participantDb = testEnv.authenticatedContext(PARTICIPANT_UID).firestore();
  await assertSucceeds(participantDb.collection("meetings").doc("m_test_123").get());
});

test("Unauthorized user: CANNOT read meeting they are not a part of", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const intruderDb = testEnv.authenticatedContext(UNAUTHORIZED_UID).firestore();
  await assertFails(intruderDb.collection("meetings").doc("m_test_123").get());
});

// ==========================================
// 4. UPDATE SCENARIOS
// ==========================================
test("Organizer: can update meeting details (title, notes, duration)", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const organizerDb = testEnv.authenticatedContext(ORGANIZER_UID).firestore();
  await assertSucceeds(
    organizerDb.collection("meetings").doc("m_test_123").update({
      title: "Updated Sprint Review Title",
      durationMinutes: 45,
    })
  );
});

test("Organizer: CANNOT change organizerUid", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const organizerDb = testEnv.authenticatedContext(ORGANIZER_UID).firestore();
  await assertFails(
    organizerDb.collection("meetings").doc("m_test_123").update({
      organizerUid: UNAUTHORIZED_UID,
    })
  );
});

test("Participant: CAN update status to Confirmed or Declined", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const participantDb = testEnv.authenticatedContext(PARTICIPANT_UID).firestore();
  await assertSucceeds(
    participantDb.collection("meetings").doc("m_test_123").update({
      status: "Declined",
    })
  );
});

test("Participant: CANNOT modify meeting details (title, notes, duration)", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const participantDb = testEnv.authenticatedContext(PARTICIPANT_UID).firestore();
  await assertFails(
    participantDb.collection("meetings").doc("m_test_123").update({
      title: "Hacked Meeting Title",
    })
  );
});

test("Participant: CANNOT modify participant membership", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const participantDb = testEnv.authenticatedContext(PARTICIPANT_UID).firestore();
  await assertFails(
    participantDb.collection("meetings").doc("m_test_123").update({
      participantUids: [PARTICIPANT_UID, UNAUTHORIZED_UID],
    })
  );
});

test("Arbitrary user: CANNOT update meeting or add themselves to participants", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const intruderDb = testEnv.authenticatedContext(UNAUTHORIZED_UID).firestore();
  await assertFails(
    intruderDb.collection("meetings").doc("m_test_123").update({
      participantUids: [ORGANIZER_UID, PARTICIPANT_UID, UNAUTHORIZED_UID],
    })
  );
});

// ==========================================
// 5. DELETION SCENARIOS
// ==========================================
test("Organizer: can delete meeting", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const organizerDb = testEnv.authenticatedContext(ORGANIZER_UID).firestore();
  await assertSucceeds(
    organizerDb.collection("meetings").doc("m_test_123").delete()
  );
});

test("Participant: CANNOT delete meeting", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const participantDb = testEnv.authenticatedContext(PARTICIPANT_UID).firestore();
  await assertFails(
    participantDb.collection("meetings").doc("m_test_123").delete()
  );
});

test("Unauthorized user: CANNOT delete meeting", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("meetings").doc("m_test_123").set(getValidMeetingData());
  });

  const intruderDb = testEnv.authenticatedContext(UNAUTHORIZED_UID).firestore();
  await assertFails(
    intruderDb.collection("meetings").doc("m_test_123").delete()
  );
});
