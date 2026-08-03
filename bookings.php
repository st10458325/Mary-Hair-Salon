<?php
/**
 * bookings.php — Mary Hair salon "My Bookings"
 * ---------------------------------------------------------------
 * No database yet, so this renders mock sample bookings — swap
 * $bookings for a real query later; render_booking_card() and the
 * markup below don't need to change.
 * ---------------------------------------------------------------
 */
require_once __DIR__ . '/components/hero.php';
require_once __DIR__ . '/components/booking-card.php';

$bookings = [
    ['service' => 'Knotless Braids',     'date' => '14 Aug 2026', 'time' => '13:00', 'type' => 'Outcall', 'location' => '22 Long St, Cape Town', 'price' => '550.00', 'status' => 'upcoming'],
    ['service' => 'Classic Bob Cut',     'date' => '21 Aug 2026', 'time' => '10:00', 'type' => 'Incall',  'location' => '', 'price' => '280.00', 'status' => 'upcoming'],
    ['service' => 'Kids Cornrows',       'date' => '02 Jul 2026', 'time' => '11:00', 'type' => 'Outcall', 'location' => '8 Kloof Rd, Cape Town', 'price' => '220.00', 'status' => 'completed'],
    ['service' => 'Lace Front Wig Install', 'date' => '18 Jun 2026', 'time' => '14:00', 'type' => 'Incall', 'location' => '', 'price' => '650.00', 'status' => 'completed'],
    ['service' => 'Box Braids',          'date' => '30 May 2026', 'time' => '09:00', 'type' => 'Outcall', 'location' => '5 Church St, Cape Town', 'price' => '450.00', 'status' => 'cancelled'],
];

$statusOrder = ['upcoming' => 0, 'completed' => 1, 'cancelled' => 2];
usort($bookings, fn($a, $b) => $statusOrder[$a['status']] <=> $statusOrder[$b['status']]);

$counts = ['all' => count($bookings), 'upcoming' => 0, 'completed' => 0, 'cancelled' => 0];
foreach ($bookings as $b) { $counts[$b['status']]++; }
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>My Bookings — Mary Hair salon</title>
<link rel="stylesheet" href="style.css">
</head>
<body>

<?php include __DIR__ . '/components/navigation.php'; ?>

<?php render_hero([
    'eyebrow' => 'Your Account',
    'title'   => 'My Bookings',
    'text'    => 'This page is showing sample data for now — it\'ll switch to your real bookings once accounts go live.',
    'compact' => true,
]); ?>

<main class="bookings-page">
  <div class="bookings-page__inner">

    <div class="bookings-filter" id="bookingsFilter">
      <button type="button" class="bookings-filter__pill is-active" data-status-filter="all">All <span><?= $counts['all'] ?></span></button>
      <button type="button" class="bookings-filter__pill" data-status-filter="upcoming">Upcoming <span><?= $counts['upcoming'] ?></span></button>
      <button type="button" class="bookings-filter__pill" data-status-filter="completed">Completed <span><?= $counts['completed'] ?></span></button>
      <button type="button" class="bookings-filter__pill" data-status-filter="cancelled">Cancelled <span><?= $counts['cancelled'] ?></span></button>
    </div>

    <?php if (empty($bookings)): ?>
      <p class="bookings-empty">No bookings yet — once you book a service, it'll show up here.</p>
    <?php else: ?>
      <div class="bookings-grid" id="bookingsGrid">
        <?php foreach ($bookings as $booking): ?>
          <div class="bookings-grid__item" data-status="<?= htmlspecialchars($booking['status']) ?>">
            <?php render_booking_card($booking); ?>
          </div>
        <?php endforeach; ?>
      </div>
    <?php endif; ?>

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

  .bookings-empty{
    text-align: center;
    padding: 60px 20px;
    color: var(--text-faint);
    font-size: 14px;
  }

  @media (max-width: 640px){
    .bookings-page{ padding: 10px 20px 70px; }
  }
</style>

<script>
(function(){
  const filter = document.getElementById('bookingsFilter');
  const items = document.querySelectorAll('#bookingsGrid .bookings-grid__item');
  if(!filter) return;

  filter.addEventListener('click', (e) => {
    const pill = e.target.closest('[data-status-filter]');
    if(!pill) return;

    filter.querySelectorAll('.bookings-filter__pill').forEach(p => p.classList.remove('is-active'));
    pill.classList.add('is-active');

    const status = pill.dataset.statusFilter;
    items.forEach(item => {
      item.hidden = status !== 'all' && item.dataset.status !== status;
    });
  });
})();
</script>

</body>
</html>
