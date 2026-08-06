<?php
/**
 * bookings.php — Mary Hair salon "My Bookings"
 * ---------------------------------------------------------------
 * Bookings are fetched client-side from Firestore for the logged-in
 * user (PHP can't reach Firestore without the Admin SDK). This file
 * just lays out the shell; components/booking-card.php is still
 * require_once'd purely for its <style> block — the card markup
 * itself is built in JS below so it can be re-rendered on cancel.
 * ---------------------------------------------------------------
 */
require_once __DIR__ . '/components/hero.php';
require_once __DIR__ . '/components/booking-card.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>My Bookings — Mary Hair salon</title>
<link rel="stylesheet" href="style.css">
<link rel="modulepreload" href="https://www.gstatic.com/firebasejs/12.17.0/firebase-app.js">
<link rel="modulepreload" href="https://www.gstatic.com/firebasejs/12.17.0/firebase-auth.js">
<link rel="modulepreload" href="https://www.gstatic.com/firebasejs/12.17.0/firebase-firestore.js">
</head>
<body>

<?php include __DIR__ . '/components/navigation.php'; ?>

<?php render_hero([
    'eyebrow' => 'Your Account',
    'title'   => 'My Bookings',
    'text'    => 'All your appointments, in one place.',
    'compact' => true,
]); ?>

<main class="bookings-page">
  <div class="bookings-page__inner">

    <div class="bookings-filter" id="bookingsFilter">
      <button type="button" class="bookings-filter__pill is-active" data-status-filter="all">All <span id="countAll">0</span></button>
      <button type="button" class="bookings-filter__pill" data-status-filter="pending">Pending <span id="countPending">0</span></button>
      <button type="button" class="bookings-filter__pill" data-status-filter="upcoming">Upcoming <span id="countUpcoming">0</span></button>
      <button type="button" class="bookings-filter__pill" data-status-filter="completed">Completed <span id="countCompleted">0</span></button>
      <button type="button" class="bookings-filter__pill" data-status-filter="cancelled">Cancelled <span id="countCancelled">0</span></button>
    </div>

    <p class="bookings-status" id="bookingsLoading">Loading your bookings…</p>
    <p class="bookings-empty" id="bookingsEmpty" hidden>No bookings yet — once you book a service, it'll show up here.</p>
    <p class="bookings-status" id="bookingsLoggedOut" hidden>Please <a href="login.php">log in</a> to see your bookings.</p>

    <div class="bookings-grid" id="bookingsGrid"></div>


  </div>
</main>

<?php include __DIR__ . '/components/booking-modal.php'; ?>
<?php include __DIR__ . '/components/auth-modal.php'; ?>
<?php include __DIR__ . '/components/footer.php'; ?>

<style>
  .bookings-page{ padding: 10px 40px 90px; }
  .bookings-page__inner{ max-width: 1000px; margin: 0 auto; }

  .bookings-filter{
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    margin-bottom: 28px;
  }
  .bookings-filter__pill{
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 9px 16px;
    border-radius: 999px;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    color: var(--text-dim);
    font-size: 12.5px;
    font-weight: 600;
    transition: background .2s ease, border-color .2s ease, color .2s ease;
  }
  .bookings-filter__pill span{
    font-size: 10.5px;
    padding: 1px 7px;
    border-radius: 999px;
    background: rgba(255,255,255,0.08);
  }
  .bookings-filter__pill:hover{ color: var(--text); }
  .bookings-filter__pill.is-active{
    background: var(--orange-soft);
    border-color: var(--orange);
    color: var(--text);
  }
  .bookings-filter__pill.is-active span{ background: var(--orange); color: #fff; }

  .bookings-grid{
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 18px;
  }
  .bookings-grid__item[hidden]{ display: none; }

  .bookings-empty, .bookings-status{
    text-align: center;
    padding: 60px 20px;
    color: var(--text-faint);
    font-size: 14px;
  }
  .bookings-status a{ color: var(--orange-light); font-weight: 600; }

  @media (max-width: 640px){
    .bookings-page{ padding: 10px 20px 70px; }
  }
</style>

<script type="module">
  import { auth, db } from '/assets/js/firebase-config.js';
  import { onAuthStateChanged } from "https://www.gstatic.com/firebasejs/12.17.0/firebase-auth.js";
  import { collection, query, where, orderBy, getDocs, doc, updateDoc } from "https://www.gstatic.com/firebasejs/12.17.0/firebase-firestore.js";

  const grid = document.getElementById('bookingsGrid');
  const loading = document.getElementById('bookingsLoading');
  const empty = document.getElementById('bookingsEmpty');
  const loggedOut = document.getElementById('bookingsLoggedOut');
  const filterBar = document.getElementById('bookingsFilter');
  const counts = { all: 0, pending: 0, upcoming: 0, completed: 0, cancelled: 0 };
  let currentBookings = [];

  function escapeHtml(str){
    const div = document.createElement('div');
    div.textContent = str ?? '';
    return div.innerHTML;
  }

  function statusLabel(status){
    return { pending: 'Pending', upcoming: 'Upcoming', completed: 'Completed', cancelled: 'Cancelled' }[status] || status;
  }

  function bookingCardHtml(b){
    const status = b.status || 'upcoming';
    const location = (b.type === 'Outcall' && b.location) ? ` — ${escapeHtml(b.location)}` : '';
    const actions = (status === 'upcoming' || status === 'pending') ? `
      <div class="booking-card__actions">
        <button type="button" class="booking-card__link" data-reschedule="${b.id}">Reschedule</button>
        <button type="button" class="booking-card__link booking-card__link--danger" data-cancel="${b.id}">Cancel</button>
      </div>` : '';

    return `
      <div class="bookings-grid__item" data-status="${status}">
        <article class="booking-card glass booking-card--${status}">
          <div class="booking-card__top">
            <h3>${escapeHtml(b.service || 'Service')}</h3>
            <span class="booking-card__status">${statusLabel(status)}</span>
          </div>
          <div class="booking-card__meta">
            <span><svg width="14" height="14" viewBox="0 0 14 14" fill="none"><rect x="1" y="2.5" width="12" height="10.5" rx="1.3" stroke="currentColor" stroke-width="1.2"/><path d="M1 5.5H13" stroke="currentColor" stroke-width="1.2"/></svg>${escapeHtml(b.dateFormatted || b.date || '')}</span>
            <span><svg width="14" height="14" viewBox="0 0 14 14" fill="none"><circle cx="7" cy="7" r="5.5" stroke="currentColor" stroke-width="1.2"/><path d="M7 4v3l2 1.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>${escapeHtml(b.time || '')}</span>
            <span><svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 1.5c-2.5 0-4.2 1.9-4.2 4.3 0 3 4.2 6.7 4.2 6.7s4.2-3.7 4.2-6.7c0-2.4-1.7-4.3-4.2-4.3z" stroke="currentColor" stroke-width="1.2"/></svg>${escapeHtml(b.type || 'Outcall')}${location}</span>
          </div>
          <div class="booking-card__bottom">
            <span class="booking-card__price">${b.price ? 'R' + escapeHtml(b.price) : ''}</span>
            ${actions}
          </div>
        </article>
      </div>
    `;
  }

  function updateCountsUI(){
    document.getElementById('countAll').textContent = counts.all;
    document.getElementById('countUpcoming').textContent = counts.upcoming;
    document.getElementById('countCompleted').textContent = counts.completed;
    document.getElementById('countCancelled').textContent = counts.cancelled;
    document.getElementById('countPending').textContent = counts.pending;
  }

  async function loadBookings(uid){
    loading.hidden = false;
    empty.hidden = true;
    grid.innerHTML = '';

    try {
      const q = query(collection(db, 'bookings'), where('uid', '==', uid), orderBy('date', 'desc'));
      const snap = await getDocs(q);

      const statusOrder = { pending: 0, upcoming: 1, completed: 2, cancelled: 3 };
      const bookings = [];
      snap.forEach(docSnap => bookings.push({ id: docSnap.id, ...docSnap.data() }));
      bookings.sort((a, b) => (statusOrder[a.status] ?? 3) - (statusOrder[b.status] ?? 3));
      currentBookings = bookings;

      counts.all = bookings.length;
      counts.pending = bookings.filter(b => b.status === 'pending').length;
      counts.upcoming = bookings.filter(b => b.status === 'upcoming').length;
      counts.completed = bookings.filter(b => b.status === 'completed').length;
      counts.cancelled = bookings.filter(b => b.status === 'cancelled').length;
      updateCountsUI();

      if(bookings.length === 0){
        empty.hidden = false;
      } else {
        grid.innerHTML = bookings.map(bookingCardHtml).join('');
      }
    } catch(err){
      console.error(err);
      grid.innerHTML = '<p class="bookings-status">Something went wrong loading your bookings. Please refresh.</p>';
    } finally {
      loading.hidden = true;
    }
  }

  onAuthStateChanged(auth, (user) => {
    if(user){
      loggedOut.hidden = true;
      loadBookings(user.uid);
    } else {
      loading.hidden = true;
      grid.innerHTML = '';
      empty.hidden = true;
      loggedOut.hidden = false;
      updateCountsUI();
    }
  });

  filterBar.addEventListener('click', (e) => {
    const pill = e.target.closest('[data-status-filter]');
    if(!pill) return;
    filterBar.querySelectorAll('.bookings-filter__pill').forEach(p => p.classList.remove('is-active'));
    pill.classList.add('is-active');
    const status = pill.dataset.statusFilter;
    grid.querySelectorAll('.bookings-grid__item').forEach(item => {
      item.hidden = status !== 'all' && item.dataset.status !== status;
    });
  });

  grid.addEventListener('click', async (e) => {
    if(e.target.closest('[data-reschedule]')){
      const id = e.target.closest('[data-reschedule]').dataset.reschedule;
      const booking = currentBookings.find(b => b.id === id);
      if(!booking){
        alert('Could not find that booking. Please refresh and try again.');
        return;
      }
      if(typeof window.openBookingModal === 'function'){
        window.openBookingModal(null, booking);
      }
      return;
    }
    const cancelBtn = e.target.closest('[data-cancel]');
    if(!cancelBtn) return;
    if(!confirm('Cancel this booking?')) return;
    try {
      await updateDoc(doc(db, 'bookings', cancelBtn.dataset.cancel), { status: 'cancelled' });
      if(auth.currentUser) loadBookings(auth.currentUser.uid);
    } catch(err){
      console.error(err);
      alert('Could not cancel this booking. Please try again.');
    }
  });
</script>

</body>
</html>