<?php
/**
 * components/booking-card.php
 * ---------------------------------------------------------------
 * One glass card summarising a booking, for bookings.php.
 * Currently fed with mock data (no database yet) — once real
 * bookings exist, bookings.php just swaps its data source; this
 * component's shape doesn't need to change.
 *
 * Usage:
 *   require_once __DIR__ . '/components/booking-card.php';
 *   render_booking_card($booking);
 *
 * Expected $booking keys:
 *   service    string   e.g. "Box Braids"
 *   date       string   e.g. "12 Aug 2026"
 *   time       string   e.g. "13:00"
 *   type       string   "Incall" | "Outcall"
 *   location   string   address, shown only if type is Outcall
 *   price      string   e.g. "R450.00"
 *   status     string   "upcoming" | "completed" | "cancelled"
 * ---------------------------------------------------------------
 */

if (!function_exists('render_booking_card')) {

    function booking_status_label(string $status): string
    {
        return [
            'upcoming'  => 'Upcoming',
            'completed' => 'Completed',
            'cancelled' => 'Cancelled',
        ][$status] ?? ucfirst($status);
    }

    function render_booking_card(array $booking): void
    {
        $service  = htmlspecialchars($booking['service']  ?? 'Service');
        $date     = htmlspecialchars($booking['date']     ?? '');
        $time     = htmlspecialchars($booking['time']     ?? '');
        $type     = htmlspecialchars($booking['type']     ?? 'Outcall');
        $location = htmlspecialchars($booking['location'] ?? '');
        $price    = htmlspecialchars($booking['price']    ?? '');
        $status   = $booking['status'] ?? 'upcoming';
        ?>
        <article class="booking-card glass booking-card--<?= htmlspecialchars($status) ?>">
            <div class="booking-card__top">
                <h3><?= $service ?></h3>
                <span class="booking-card__status"><?= booking_status_label($status) ?></span>
            </div>

            <div class="booking-card__meta">
                <span><svg width="14" height="14" viewBox="0 0 14 14" fill="none"><rect x="1" y="2.5" width="12" height="10.5" rx="1.3" stroke="currentColor" stroke-width="1.2"/><path d="M1 5.5H13" stroke="currentColor" stroke-width="1.2"/></svg><?= $date ?></span>
                <span><svg width="14" height="14" viewBox="0 0 14 14" fill="none"><circle cx="7" cy="7" r="5.5" stroke="currentColor" stroke-width="1.2"/><path d="M7 4v3l2 1.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg><?= $time ?></span>
                <span><svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 1.5c-2.5 0-4.2 1.9-4.2 4.3 0 3 4.2 6.7 4.2 6.7s4.2-3.7 4.2-6.7c0-2.4-1.7-4.3-4.2-4.3z" stroke="currentColor" stroke-width="1.2"/></svg><?= $type ?><?= ($type === 'Outcall' && $location !== '') ? ' — ' . $location : '' ?></span>
            </div>

            <div class="booking-card__bottom">
                <span class="booking-card__price"><?= $price !== '' ? 'R' . $price : '' ?></span>
                <?php if ($status === 'upcoming'): ?>
                    <div class="booking-card__actions">
                        <button type="button" class="booking-card__link">Reschedule</button>
                        <button type="button" class="booking-card__link booking-card__link--danger">Cancel</button>
                    </div>
                <?php endif; ?>
            </div>
        </article>
        <?php
    }
}
?>
<style>
  .booking-card{
    border-radius: var(--radius-lg);
    padding: 20px 22px;
    display: flex;
    flex-direction: column;
    gap: 14px;
  }
  .booking-card--cancelled{ opacity: 0.55; }

  .booking-card__top{
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }
  .booking-card__top h3{
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 600;
  }
  .booking-card__status{
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.3px;
    padding: 5px 12px;
    border-radius: 999px;
    background: var(--orange-soft);
    color: var(--orange-light);
    white-space: nowrap;
  }
  .booking-card--completed .booking-card__status{ background: rgba(255,255,255,0.08); color: var(--text-dim); }
  .booking-card--cancelled .booking-card__status{ background: rgba(224,71,90,0.15); color: #e0475a; }

  .booking-card__meta{
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
    font-size: 12.5px;
    color: var(--text-dim);
  }
  .booking-card__meta span{ display: flex; align-items: center; gap: 6px; }
  .booking-card__meta svg{ color: var(--orange-light); flex-shrink: 0; }

  .booking-card__bottom{
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 10px;
    border-top: 1px solid var(--glass-border);
  }
  .booking-card__price{ font-size: 15px; font-weight: 700; }
  .booking-card__actions{ display: flex; gap: 14px; }
  .booking-card__link{
    background: none; border: none;
    font-size: 12.5px; font-weight: 600;
    color: var(--text-dim);
  }
  .booking-card__link:hover{ color: var(--orange-light); }
  .booking-card__link--danger:hover{ color: #e0475a; }
</style>
