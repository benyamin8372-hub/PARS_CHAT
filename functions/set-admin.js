const admin = require("firebase-admin");

async function main() {
  const uid = process.argv[2];
  if (!uid) {
    console.error("Usage: npm run set-admin -- FIREBASE_AUTH_UID");
    process.exit(2);
  }
  admin.initializeApp({ credential: admin.credential.applicationDefault() });
  await admin.auth().setCustomUserClaims(uid, { admin: true });
  console.log("Admin claim granted to:", uid);
  process.exit(0);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
