// The app only keeps one running total (installs/counter), so installs per day and per hour come
// from saving that total at the start of every Pakistan-time hour, in installDaily/{YYYY-MM-DD}:
//   h00..h23  = the total when that hour started
//   endTotal  = the latest total for that day (closed by the midnight run)
const PKT_OFFSET_MS = 5 * 60 * 60 * 1000; // Pakistan has no daylight saving

function pktParts(ms) {
  const iso = new Date(ms + PKT_OFFSET_MS).toISOString();
  return { date: iso.slice(0, 10), hour: iso.slice(11, 13) };
}

/** The installDaily writes for one hourly run: today's row, plus closing yesterday at midnight. */
export function installSnapshotWrites(scheduledMs, total) {
  const updatedAt = new Date(scheduledMs).toISOString();
  const { date, hour } = pktParts(scheduledMs);
  const writes = [
    { date, fields: { date, endTotal: total, [`h${hour}`]: total, updatedAt } },
  ];
  if (hour === "00") {
    const previous = pktParts(scheduledMs - 60 * 60 * 1000).date;
    writes.push({ date: previous, fields: { endTotal: total, updatedAt } });
  }
  return writes;
}
