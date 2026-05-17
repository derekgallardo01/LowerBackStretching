// Cloud Functions for Lower Back Stretching.
//
// The mobile clients write directly to Firestore via the SDK — most
// per-user logic is enforced by firestore.rules, not server code.
// Functions here cover the cross-user pieces:
//
//   * Friend leaderboards: a scheduled aggregator that walks each
//     user's sessions/ subcollection and writes a public stats doc
//     (totalSessions, currentStreak, level) under /users/{uid}/public.
//   * Buddy-room cleanup: a scheduled job that deletes
//     /buddyRooms/{roomId} docs older than 24h.
//
// Both are wired but stubbed out; they're no-ops until enabled in the
// console.

const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { logger } = require("firebase-functions");

initializeApp();

const db = getFirestore();

/**
 * Daily aggregator. Walks every user, computes their public stats
 * snapshot, and writes /users/{uid}/public/stats. Friends can read
 * that public doc to render leaderboards without exposing the
 * per-session data.
 */
exports.aggregatePublicStats = onSchedule("every 24 hours", async (event) => {
  logger.info("aggregatePublicStats — placeholder; implement when leaderboards ship.");
});

/**
 * Buddy-room TTL. Deletes buddy-mode shared docs older than 24h
 * so the collection doesn't grow unbounded.
 */
exports.cleanupBuddyRooms = onSchedule("every 6 hours", async (event) => {
  logger.info("cleanupBuddyRooms — placeholder; implement when buddy mode ships.");
});
